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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import static utils.CommonUtils.EVNFM_URI;
import static utils.CommonUtils.VNFLCM_URI;
import static utils.CommonUtils.createMockResponse;
import static utils.CommonUtils.createNfvoConfig;
import static utils.CommonUtils.createOnboardingConfig;
import static utils.CommonUtils.createRoute;
import static utils.CommonUtils.createVnfmConfig;
import static utils.CommonUtils.parseJsonFile;
import static utils.CommonUtils.postMockServerHttpRequest;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.eoevnfmnbi.config.NfvoConfig;
import com.ericsson.eoevnfmnbi.config.OnboardingConfig;
import com.ericsson.eoevnfmnbi.config.SpringExtensionTestConfig;
import com.ericsson.eoevnfmnbi.config.VnfmConfig;
import com.ericsson.eoevnfmnbi.exceptions.ConnectionFailureException;
import com.ericsson.eoevnfmnbi.filters.LcmRouterGatewayFilterFactory;
import com.ericsson.eoevnfmnbi.models.Config;
import com.ericsson.eoevnfmnbi.security.CustomX509TrustManager;
import com.ericsson.eoevnfmnbi.utils.Constants;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.micrometer.observation.ObservationRegistry;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;

@ContextConfiguration(classes = {LcmRouterGatewayFilterFactory.class, CustomX509TrustManager.class, HazelcastAutoConfiguration.class,
    SpringExtensionTestConfig.class })
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@TestPropertySource(properties = {"truststore.path = ${java.home}/lib/security/cacerts",
    "truststore.pass = changeit", "retry.maxAttempts=2", "retry.backoff=3"})
public class LcmRouterGatewayFilterFactoryCreateIdentifierTest {

    private static MockServerWebExchange exchange;
    private static MockWebServer mockWebServer;

    private static Config config = new Config();
    private static final String ORCHESTRATOR_PATH = "/vnflcm/v1/vnf_instances";
    private static final String VNF_DESCRIPTOR_ID = "d3def1ce-4cf4-477c";
    private static String onboardingHost;

    private IMap<Object, Object> vnfDescriptors;

    @Spy
    private LcmRouterGatewayFilterFactory lcmRouterGatewayFilterFactory;

    @MockBean
    private GatewayFilterChain gatewayFilterChain;

    @MockBean
    private ObservationRegistry observationRegistry;

    @Mock
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private ConnectionProvider customConnectionProvider;

    @BeforeAll
    public static void mockServerSetup() {
        mockWebServer = new MockWebServer();
        onboardingHost = String.valueOf(mockWebServer.url("/"));
        config.setUrl(VNFLCM_URI);
    }

    @BeforeEach
    public void setup() {
        String requestBody = "{\"vnfdId\": \"d3def1ce-4cf4-477c\", \"vnfInstanceName\": \"my-sample-release\"}";
        MockServerHttpRequest mockServerHttpRequest = postMockServerHttpRequest(ORCHESTRATOR_PATH, requestBody);

        exchange = MockServerWebExchange.from(mockServerHttpRequest);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, createRoute("Evnfm", 1, EVNFM_URI));

        VnfmConfig vnfmConfig = createVnfmConfig("");
        OnboardingConfig onboardingConfig = createOnboardingConfig(onboardingHost, false);
        NfvoConfig nfvoConfig = createNfvoConfig(false);

        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "vnfmConfig", vnfmConfig);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "hazelcastInstance", hazelcastInstance);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "onboardingConfig", onboardingConfig);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "nfvoConfig", nfvoConfig);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "customConnectionProvider", customConnectionProvider);


        Set<HazelcastInstance> allHazelcastInstances = Hazelcast.getAllHazelcastInstances();
        HazelcastInstance realInstance = allHazelcastInstances.stream().findFirst().get();
        vnfDescriptors = realInstance.getMap(Constants.VNF_DESCRIPTOR);

        when(hazelcastInstance.getMap(anyString())).thenReturn(vnfDescriptors);
        when(gatewayFilterChain.filter(Mockito.any())).thenReturn(Mono.empty());
    }

    @AfterEach
    public void eachTestCleanUp() {
        vnfDescriptors.evictAll();
    }

    @Test
    public void createIdentifierRouteToEvnfmWithoutCacheTest() {
        String jsonResponseBody = parseJsonFile("packagesResponseFromEvnfm.json");
        mockWebServer.enqueue(createMockResponse(HttpStatus.ACCEPTED, jsonResponseBody));
        lcmRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }

    @Test
    public void createIdentifierRouteToVnflcmWithoutCacheTest() {
        String jsonResponseBody = parseJsonFile("packagesResponseFromVnflcm.json");
        mockWebServer.enqueue(createMockResponse(HttpStatus.ACCEPTED, jsonResponseBody));
        lcmRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }

    @Test
    public void createIdentifierRouteToEvnfmWithCacheTest() {
        vnfDescriptors.put(VNF_DESCRIPTOR_ID, Constants.CNF);
        lcmRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }

    @Test
    public void createIdentifierRouteToVnflcmWithCacheTest() {
        vnfDescriptors.put(VNF_DESCRIPTOR_ID, Constants.VNF);
        lcmRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }

    @Test
    public void createIdentifierRouteToVnflcmGetPackageFailTestTest() {
        mockWebServer.enqueue(createMockResponse(HttpStatus.ACCEPTED, "[]"));
        lcmRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }

    @Test
    public void createIdentifierRouteConnectionFailureTest() {
        String onboardingHostNonReachable = "http://locahost";
        OnboardingConfig onboardingConfig = createOnboardingConfig(onboardingHostNonReachable, false);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "onboardingConfig", onboardingConfig);

        ConnectionFailureException exception =
                Assertions.assertThrows(ConnectionFailureException.class, () ->
                        lcmRouterGatewayFilterFactory
                                .apply(config)
                                .filter(exchange, gatewayFilterChain)
                                .block());

        Map<String, Object> errorAttributes = exception.getErrorAttributes();

        assertEquals(HttpStatus.BAD_REQUEST.value(), errorAttributes.get("status"));
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorAttributes.get("type"));
        Assertions.assertTrue(errorAttributes.get("detail").toString().contains("Cannot get package details. Details: "));
    }
}
