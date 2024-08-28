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
package com.ericsson.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;


public class KubernetesJavaClientTest {

    private static final Logger LOGGER = getLogger(KubernetesJavaClientTest.class);

    private static final String NAMESPACE = System.getProperty("namespace");

    private static final String SECRET_NAME = "eric-eo-evnfm-nfvo";
    private static final String CONFIG_MAP_NAME = "eric-eo-evnfm-nfvo-config";
    private static final String SOME_DATA = "some-data";

    private static KubernetesClient client = null;

    @BeforeAll
    public static void setup() throws IOException {
        client = new KubernetesClientBuilder().build();
    }

    @AfterAll
    public static void teardown() {
        deleteSecret();
        deleteConfigMap();

        checkResourcesDoNotExistInNamespace();
    }

    @Test
    @DisplayName("Check Kubernetes Java client works with SLES image")
    public void testClusterConnection() {
        LOGGER.info("Checking secret and configMap are not present in namespace before starting test");
        checkResourcesDoNotExistInNamespace();

        createSecret();
        createConfigMap();

        LOGGER.info("Checking secret and configMap are present as expected");
        checkResourcesExistInNamespace();
    }

    private static void createSecret() {

        LOGGER.info("Creating NFVO secret");

        client.secrets()
                .inNamespace(NAMESPACE)
                .resource(new SecretBuilder().withNewMetadata()
                                  .withName(SECRET_NAME).endMetadata()
                                  .withType("generic")
                                  .build()
                )
                .create();

    }

    private static void deleteSecret() {
        client.secrets().inNamespace(NAMESPACE).withName(SECRET_NAME).delete();
    }

    private static void createConfigMap() {
        LOGGER.info("Creating NFVO configmap");
        client.configMaps()
                .inNamespace(NAMESPACE).resource(new ConfigMapBuilder().withNewMetadata()
                                                         .withName(CONFIG_MAP_NAME)
                                                         .endMetadata()
                                                         .addToData("application.yaml", SOME_DATA)
                                                         .build()
                )
                .create();
    }

    private static void deleteConfigMap() {

        LOGGER.info("Deleting NFVO configmap");
        client.configMaps().inNamespace(NAMESPACE).withName(CONFIG_MAP_NAME).delete();
    }

    private static void checkResourcesExistInNamespace() {
        // Need to allow time for service to read config update and restart
        await().pollDelay(3000, TimeUnit.MILLISECONDS).until(() -> true);

        assertThat(isConfigMapPresentInNamespace(CONFIG_MAP_NAME))
                .withFailMessage("Configmap missing in namespace when it is expected to be present")
                .isTrue();
        assertThat(isSecretPresentInNamespace(SECRET_NAME))
                .withFailMessage("Secret missing in namespace when it is expected to be present")
                .isTrue();
    }

    private static void checkResourcesDoNotExistInNamespace() {
        // Need to allow time for service to read config update and restart
        await().pollDelay(3000, TimeUnit.MILLISECONDS).until(() -> true);

        assertThat(isConfigMapPresentInNamespace(CONFIG_MAP_NAME))
                .withFailMessage("Configmap is present in namespace when it shouldn't be")
                .isFalse();
        assertThat(isSecretPresentInNamespace(SECRET_NAME))
                .withFailMessage("Secret is present in namespace when it shouldn't be")
                .isFalse();
    }

    private static boolean isSecretPresentInNamespace(final String secretName) {

        SecretList secretList = client.secrets().inNamespace(NAMESPACE).list();
        LOGGER.info("Check if secret exists in namespace");
        for (Secret item : secretList.getItems()) {
            if (item.getMetadata().getName().equals(secretName)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isConfigMapPresentInNamespace(String configMapName) {
        ConfigMapList configMapList = client.configMaps().inNamespace(NAMESPACE).list();
        LOGGER.info("Check if configMap exists in namespace");
        for (ConfigMap item : configMapList.getItems()) {
            if (item.getMetadata().getName().equals(configMapName)) {
                return true;
            }
        }

        return false;
    }
}
