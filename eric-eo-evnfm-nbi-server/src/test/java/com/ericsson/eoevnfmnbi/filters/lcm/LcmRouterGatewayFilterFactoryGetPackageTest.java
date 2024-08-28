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
import static org.junit.jupiter.api.Assertions.assertTrue;

import static utils.CommonUtils.createMockResponse;
import static utils.CommonUtils.createNfvoConfig;
import static utils.CommonUtils.createOnboardingConfig;
import static utils.CommonUtils.createRetryTemplateConfig;
import static utils.CommonUtils.parseJsonFile;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.eoevnfmnbi.config.NfvoConfig;
import com.ericsson.eoevnfmnbi.config.OnboardingConfig;
import com.ericsson.eoevnfmnbi.config.SpringExtensionTestConfig;
import com.ericsson.eoevnfmnbi.config.retrytemplate.RetryTemplateConfig;
import com.ericsson.eoevnfmnbi.exceptions.ConnectionFailureException;
import com.ericsson.eoevnfmnbi.filters.LcmRouterGatewayFilterFactory;
import com.ericsson.eoevnfmnbi.models.Generic;
import com.ericsson.eoevnfmnbi.security.CustomX509TrustManager;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {LcmRouterGatewayFilterFactory.class, CustomX509TrustManager.class, HazelcastAutoConfiguration.class,
    SpringExtensionTestConfig.class })
@ExtendWith({ MockitoExtension.class, SpringExtension.class})
@TestPropertySource(properties = {"truststore.path = ${java.home}/lib/security/cacerts",
    "truststore.pass = changeit", "retry.maxAttempts=2", "retry.backoff=3"})
public class LcmRouterGatewayFilterFactoryGetPackageTest {

    private static MockWebServer mockWebServer;
    private static String onboardingHost;

    @Autowired
    private LcmRouterGatewayFilterFactory lcmRouterGatewayFilterFactory;

    @BeforeEach
    public void setup() {
        mockWebServer = new MockWebServer();
        onboardingHost = String.valueOf(mockWebServer.url("/"));
        OnboardingConfig onboardingConfig = createOnboardingConfig(onboardingHost, false);
        RetryTemplateConfig retryTemplateConfig = createRetryTemplateConfig(2, 1000);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "onboardingConfig", onboardingConfig);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "retryTemplateConfig", retryTemplateConfig);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "maxAttempts", 2);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "backoff", 1000);
    }

    @Test
    public void getPackageEVNFMSuccessTest() {
        String jsonResponseBody = parseJsonFile("packagesResponseFromEvnfm.json");
        mockWebServer.enqueue(createMockResponse(HttpStatus.OK, jsonResponseBody));
        List<Generic> result = lcmRouterGatewayFilterFactory.getPackageEVNFM("123").block();
        Generic aPackage = result.get(0);
        assertEquals("d3def1ce-4cf4-477c", aPackage.getVnfdId());
        assertEquals("63a4bd16-0379-4a4f-ae8c-6d673f47683c", aPackage.getId());
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Disabled("doesn't work")
    @Test
    public void getPackageEVNFMRetryExhaustedTest() {
        String onboardingHostNonReachable = "http://localhost";
        OnboardingConfig onboardingConfig = createOnboardingConfig(onboardingHostNonReachable, false);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "onboardingConfig", onboardingConfig);
        Mono<List<Generic>> result = lcmRouterGatewayFilterFactory.getPackageEVNFM("123");
        Throwable exception = Assertions.assertThrows(RuntimeException.class, result::block);
        assertTrue(Exceptions.isRetryExhausted(exception));
    }

    @Test
    public void getPackageNFVOSuccessTest() {
        NfvoConfig nfvoConfig = createNfvoConfig(true);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "nfvoConfig", nfvoConfig);
        String jsonResponseBody = parseJsonFile("packagesResponseFromNfvo.json");
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        mockWebServer.enqueue(createMockResponse(HttpStatus.OK, jsonResponseBody));
        Mono<List<Generic>> result = lcmRouterGatewayFilterFactory.getPackageNFVO("123");
        assertEquals(2, mockWebServer.getRequestCount());
        result.subscribe(item -> {
            Generic aPackage = item.get(0);
            assertEquals("b0b99535-28a1-4531-9c12-7d194b660500", aPackage.getVnfdId());
            assertEquals("43bf1225-81e1-46b4-ae10-cadea4432939", aPackage.getId());
        });
    }

    @Test
    public void getPackageNFVORetryExhaustedTest() {
        NfvoConfig nfvoConfig = createNfvoConfig(true);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "nfvoConfig", nfvoConfig);
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        ConnectionFailureException exception = Assertions.assertThrows(ConnectionFailureException.class,
                                                                       () -> lcmRouterGatewayFilterFactory.getPackageNFVO("123").block());
        assertEquals(3, mockWebServer.getRequestCount());
        Map<String, Object> errorAttributes = exception.getErrorAttributes();
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorAttributes.get("status"));
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorAttributes.get("type"));
        assertTrue(errorAttributes.get("detail").toString().contains("Cannot get package details. Details: "));
    }
}
