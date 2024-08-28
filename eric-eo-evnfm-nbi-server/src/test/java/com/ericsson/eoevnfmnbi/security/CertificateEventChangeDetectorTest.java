/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
package com.ericsson.eoevnfmnbi.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import static com.ericsson.eoevnfmnbi.security.CustomX509TrustManager.TEMP_KEYSTORE;

import static utils.CommonUtils.getResourceContent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.kubernetes.commons.config.SecretsPropertySource;
import org.springframework.cloud.kubernetes.commons.config.SourceData;
import org.springframework.cloud.kubernetes.commons.config.reload.ConfigReloadProperties;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.eoevnfmnbi.exceptions.DefaultCustomException;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperation;
import io.fabric8.kubernetes.client.dsl.internal.OperationContext;

@ContextConfiguration(classes = { CustomX509TrustManager.class })
@TestPropertySource(properties = { "spring.cloud.kubernetes.enabled = false", "truststore.path = ${java.home}/lib/security/cacerts",
    "truststore.pass = changeit" })
@ExtendWith({ MockitoExtension.class, SpringExtension.class })
class CertificateEventChangeDetectorTest {

    public static final String KEYSTORE_PASS = "changeit";

    @Autowired
    CustomX509TrustManager customX509TrustManager;

    @Mock
    AbstractEnvironment environment;

    @MockBean
    ConfigReloadProperties properties;

    @Mock
    KubernetesClient kubernetesClient;

    @Spy
    @InjectMocks
    CertificateEventChangeDetector detector;

    @Test
    void onEvent() throws IOException, URISyntaxException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        CertificateEventChangeDetector certificateEventChangeDetector = new CertificateEventChangeDetector(
            environment, kubernetesClient, properties, customX509TrustManager);

        String certs = getResourceContent("testCert.crt");
        certificateEventChangeDetector.updateTrustManager(certs);

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(Files.newInputStream(Paths.get(System.getProperty("java.io.tmpdir") + File.separator + TEMP_KEYSTORE)),
                KEYSTORE_PASS.toCharArray());

        byte[] certsBytes = Base64.getDecoder().decode(certs);
        ByteArrayInputStream bytes = new ByteArrayInputStream(certsBytes);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> x509certs = cf.generateCertificates(bytes);

        X509Certificate testCert = (X509Certificate) x509certs.iterator().next();
        String alias = ks.getCertificateAlias(testCert);
        Assertions.assertFalse(Strings.isEmpty(alias), "Did not find cert in trustStore");
    }

    @Test
    void testFindPropertySourcesReturnEmptyList() {
        doReturn(new MutablePropertySources()).when(environment).getPropertySources();
        CertificateEventChangeDetector certificateEventChangeDetector = new CertificateEventChangeDetector(
                environment, kubernetesClient, properties, customX509TrustManager);

        List<SecretsPropertySource> propertySources = certificateEventChangeDetector.findPropertySources(SecretsPropertySource.class);

        Assertions.assertEquals(0, propertySources.size());
    }

    @Test
    void testFindPropertySourcesReturnNotEmptyList() {
        MutablePropertySources mutablePropertySources = new MutablePropertySources();
        mutablePropertySources.addFirst(new SecretsPropertySource(new SourceData("testSecret", Map.of("namespace", "test"))));
        doReturn(mutablePropertySources).when(environment).getPropertySources();
        CertificateEventChangeDetector certificateEventChangeDetector = new CertificateEventChangeDetector(
                environment, kubernetesClient, properties, customX509TrustManager);

        List<SecretsPropertySource> propertySources = certificateEventChangeDetector.findPropertySources(SecretsPropertySource.class);

        Assertions.assertEquals(1, propertySources.size());
        Assertions.assertInstanceOf(SecretsPropertySource.class, propertySources.get(0));
        Assertions.assertEquals("testSecret", propertySources.get(0).getName());
    }

    @Test
    void testReSubscription() throws NoSuchFieldException, IllegalAccessException {

        //given
        doNothing().when(detector).subscribe();
        Field initialTimeout = detector.getClass().getSuperclass().getDeclaredField("initialTimeout");
        initialTimeout.setAccessible(true);
        initialTimeout.setLong(detector, 0L);
        //when WatchConnectionManager closes it calls this method through delegate e.g. Subscriber Impl
        detector.scheduleResubscribe();

        //then
        verify(detector, timeout(TimeUnit.SECONDS.toMillis(1)).atLeastOnce()).subscribe();
    }

    @Test
    void testReSubscriptionFailedWithShutdown() throws NoSuchFieldException, IllegalAccessException {

        //given
        doNothing().when(detector).subscribe();
        doThrow(new RuntimeException()).when(detector).subscribe();
        Field initialTimeout = detector.getClass().getSuperclass().getDeclaredField("initialTimeout");
        initialTimeout.setAccessible(true);
        initialTimeout.setLong(detector, 0L);
        //when WatchConnectionManager closes it calls this method through delegate e.g. Subscriber Impl
        detector.scheduleResubscribe();

        //then
        verify(detector, timeout(TimeUnit.SECONDS.toMillis(1)).atLeastOnce()).shutdown();

    }

    @Test
    public void testSubscriptionWithNfvo() {
        MixedOperation<Secret, SecretList, Resource<Secret>> mixedOperation;
        mixedOperation = spy(new HasMetadataOperation<>(new OperationContext(), Secret.class, SecretList.class));

        doReturn(true).when(properties).monitoringSecrets();
        doReturn(mixedOperation).when(kubernetesClient).secrets();
        doReturn(mock(Watch.class)).when(mixedOperation).watch(any());
        detector.subscribe();
    }

    @Test
    public void testSubscriptionWithoutNfvo() {
        doReturn(false).when(properties).monitoringSecrets();
        detector.subscribe();
    }

    @Test
    public void testOnSecretInvocationWithIncorrectAction() {
        List<SecretsPropertySource> propertySourceList = new ArrayList<>();
        SecretsPropertySource secretsPropertySource = mock(SecretsPropertySource.class);
        propertySourceList.add(secretsPropertySource);
        Secret secret = new Secret();
        final ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName("changeDetector");
        objectMeta.setNamespace("warriors-of-the-world");
        secret.setMetadata(objectMeta);

        doReturn(propertySourceList).when(detector).findPropertySources(SecretsPropertySource.class);
        doReturn("strange_secret_name").when(secretsPropertySource).getName();

        detector.onSecret(Watcher.Action.ADDED, secret);
    }

    @Test
    public void testOnSecretInvocationWithCorrectAction() {
        List<SecretsPropertySource> propertySourceList = new ArrayList<>();
        SecretsPropertySource secretsPropertySource = mock(SecretsPropertySource.class);
        propertySourceList.add(secretsPropertySource);
        Secret secret = new Secret();
        final ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName("changeDetector");
        objectMeta.setNamespace("warriors-of-the-world");
        secret.setMetadata(objectMeta);

        doReturn(propertySourceList).when(detector).findPropertySources(SecretsPropertySource.class);
        doReturn("secrets.changeDetector.warriors-of-the-world").when(secretsPropertySource).getName();

        detector.onSecret(Watcher.Action.ADDED, secret);
    }

    @Test
    public void testOnSecretInvocationWithCorrectActionAndCertificates() {
        List<SecretsPropertySource> propertySourceList = new ArrayList<>();
        SecretsPropertySource secretsPropertySource = mock(SecretsPropertySource.class);
        propertySourceList.add(secretsPropertySource);
        Secret secret = new Secret();
        final ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName("changeDetector");
        objectMeta.setNamespace("warriors-of-the-world");
        secret.setMetadata(objectMeta);
        secret.setData(Map.of("tls.crt", "tls.crt"));

        doReturn(propertySourceList).when(detector).findPropertySources(SecretsPropertySource.class);
        doReturn("secrets.changeDetector.warriors-of-the-world").when(secretsPropertySource).getName();
        try {
            doNothing().when(detector).updateTrustManager(any());
        } catch (CertificateException | KeyStoreException e) {
            throw new RuntimeException(e);
        }

        detector.onSecret(Watcher.Action.ADDED, secret);
    }

    @Test
    public void testDefaultCustomException() {
        final DefaultCustomException exception = new DefaultCustomException("test exception");
        Assertions.assertNotNull(exception.getErrorAttributes().get("detail"));
    }
}