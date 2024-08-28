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
package com.ericsson.eoevnfmnbi.filters.lcm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static utils.CommonUtils.createNfvoConfig;
import static utils.CommonUtils.createOnboardingConfig;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.eoevnfmnbi.config.NfvoConfig;
import com.ericsson.eoevnfmnbi.config.OnboardingConfig;
import com.ericsson.eoevnfmnbi.config.SpringExtensionTestConfig;
import com.ericsson.eoevnfmnbi.filters.LcmRouterGatewayFilterFactory;
import com.ericsson.eoevnfmnbi.models.Generic;
import com.ericsson.eoevnfmnbi.security.CustomX509TrustManager;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {LcmRouterGatewayFilterFactory.class, CustomX509TrustManager.class, HazelcastAutoConfiguration.class,
    SpringExtensionTestConfig.class })
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@TestPropertySource(properties = {"truststore.path = ${java.home}/lib/security/cacerts",
    "truststore.pass = changeit", "retry.maxAttempts=2", "retry.backoff=3"})
public class LcmRouterGatewayFilterFactoryNfvoEnabledTest {

    private static NfvoConfig nfvoConfig;

    @Spy
    @Autowired
    private LcmRouterGatewayFilterFactory lcmRouterGatewayFilterFactory;

    @Mock
    private CustomX509TrustManager trustManager;

    @MockBean
    private OkHttpClient okHttpClient;

    @BeforeEach
    public void setup() {
        nfvoConfig = createNfvoConfig(true);
        OnboardingConfig onboardingConfig = createOnboardingConfig("https://ecm-service", nfvoConfig.isEnabled());
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "onboardingConfig", onboardingConfig);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "nfvoConfig", nfvoConfig);
    }

    @Test
    public void createIdentifierTest() throws IOException {
        final String packageDetailsFromNFVO = "[\n"
                + "    {\n"
                + "        \"helmPackageUrls\": [\n"
                + "            {\n"
                + "                \"chartUrl\": \"https://helm-repository.evnfm01.eccd01.eccd:443/onboarded/charts/spider-app-label-verification-2"
                + ".193.100.tgz\",\n"
                + "                \"priority\": 1\n"
                + "            }\n"
                + "        ],\n"
                + "        \"id\": \"43bf1225-81e1-46b4-ae10-cadea4432939\",\n"
                + "        \"onboardingState\": \"ONBOARDED\",\n"
                + "        \"operationalState\": \"ENABLED\",\n"
                + "        \"usageState\": \"IN_USE\",\n"
                + "        \"vnfProductName\": \"SPIDER-APP-label-ver\",\n"
                + "        \"vnfProvider\": \"Ericsson\",\n"
                + "        \"vnfSoftwareVersion\": \"1.2(CXS101289_R81E08)\",\n"
                + "        \"vnfdId\": \"b0b99535-28a1-4531-9c12-7d194b660500\",\n"
                + "        \"vnfdVersion\": \"cxp9025898_4r81e08\"\n"
                + "    }\n"
                + "]";

        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        OkHttpClient.Builder builder = mock(OkHttpClient.Builder.class);

        when(okHttpClient.newBuilder()).thenReturn(builder);
        when(builder.sslSocketFactory(any(), any())).thenReturn(builder);
        when(builder.build()).thenReturn(okHttpClient);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(packageDetailsFromNFVO);
        when(lcmRouterGatewayFilterFactory.updateSslContext()).thenReturn(okHttpClient);

        Mono<List<Generic>> result = lcmRouterGatewayFilterFactory.getPackageNFVO("123");
        result.subscribe(item -> {
            Generic aPackage = item.get(0);
            assertEquals("b0b99535-28a1-4531-9c12-7d194b660500", aPackage.getVnfdId());
            assertEquals("43bf1225-81e1-46b4-ae10-cadea4432939", aPackage.getId());
        });
    }
}
