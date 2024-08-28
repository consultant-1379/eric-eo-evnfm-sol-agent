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
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

import static com.ericsson.eoevnfmnbi.utils.Constants.VNF_INSTANCE_ID;

import static utils.CommonUtils.EVNFM_URI;
import static utils.CommonUtils.VNFLCM_URI;
import static utils.CommonUtils.VNFM_VNF_INSTANCES_ID_PATH;
import static utils.CommonUtils.createMockResponse;
import static utils.CommonUtils.createRoute;
import static utils.CommonUtils.createVnfmConfig;
import static utils.CommonUtils.getMockServerHttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.eoevnfmnbi.config.SpringExtensionTestConfig;
import com.ericsson.eoevnfmnbi.config.VnfmConfig;
import com.ericsson.eoevnfmnbi.filters.LcmRouterGatewayFilterFactory;
import com.ericsson.eoevnfmnbi.models.Config;
import com.ericsson.eoevnfmnbi.security.CustomX509TrustManager;
import com.ericsson.eoevnfmnbi.utils.Constants;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {LcmRouterGatewayFilterFactory.class, CustomX509TrustManager.class, HazelcastAutoConfiguration.class,
    SpringExtensionTestConfig.class })
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@TestPropertySource(properties = {"truststore.path = ${java.home}/lib/security/cacerts",
    "truststore.pass = changeit", "retry.maxAttempts=2", "retry.backoff=3"})
public class LcmRouterGatewayFilterFactoryLifecycleOperationsTest {

    private static MockServerWebExchange exchange;
    private static MockWebServer mockWebServer;

    private static Config config = new Config();
    private static final String INSTANCE_ID = "e3def1ce-4cf4-477c-aab3-21c454e6a389";
    private static String vnfmHost;

    private IMap<Object, Object> vnfInstances;
    private static final Map<String, String> STRING_STRING_HASH_MAP = new HashMap<>();

    @Spy
    private LcmRouterGatewayFilterFactory lcmRouterGatewayFilterFactory;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    @Mock
    private HazelcastInstance hazelcastInstance;

    @BeforeAll
    public static void mockServerSetup() {
        mockWebServer = new MockWebServer();
        vnfmHost = String.valueOf(mockWebServer.url("/"));

        // TODO find a way to extract segment from the mock httprequest
        STRING_STRING_HASH_MAP.put(VNF_INSTANCE_ID, INSTANCE_ID);
        config.setUrl(VNFLCM_URI);
    }

    @BeforeEach
    public void setup() {
        MockServerHttpRequest mockServerHttpRequest = getMockServerHttpRequest(VNFM_VNF_INSTANCES_ID_PATH, INSTANCE_ID);
        exchange = MockServerWebExchange.from(mockServerHttpRequest);
        exchange.getAttributes().put(URI_TEMPLATE_VARIABLES_ATTRIBUTE, STRING_STRING_HASH_MAP);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, createRoute("Evnfm", 1, EVNFM_URI));

        VnfmConfig vnfmConfig = createVnfmConfig(vnfmHost);

        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "vnfmConfig", vnfmConfig);
        ReflectionTestUtils.setField(lcmRouterGatewayFilterFactory, "hazelcastInstance", hazelcastInstance);

        Set<HazelcastInstance> allHazelcastInstances = Hazelcast.getAllHazelcastInstances();
        HazelcastInstance realInstance = allHazelcastInstances.stream().findFirst().get();
        vnfInstances = realInstance.getMap(Constants.VNF_INSTANCES);

        when(gatewayFilterChain.filter(Mockito.any())).thenReturn(Mono.empty());
    }

    @AfterEach
    public void eachTestCleanUp() {
        vnfInstances.evictAll();
    }

    @Test
    public void routedToEvnfmWithoutCache() {
        when(hazelcastInstance.getMap(anyString())).thenReturn(vnfInstances);
        mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "{}"));
        lcmRouterGatewayFilterFactory.apply(config)
                .filter(exchange, gatewayFilterChain)
                .block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }

    @Test
    public void routedToVnflcmWithoutCache() {
        when(hazelcastInstance.getMap(anyString())).thenReturn(vnfInstances);
        mockWebServer.enqueue(createMockResponse(HttpStatus.NOT_FOUND, "{}"));
        lcmRouterGatewayFilterFactory.apply(config)
                .filter(exchange, gatewayFilterChain)
                .block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }

    @Test
    public void routedToEvnfmWithCache() {
        when(hazelcastInstance.getMap(anyString())).thenReturn(vnfInstances);
        vnfInstances.put(INSTANCE_ID, Constants.CNF);
        lcmRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }

    @Test
    public void routedToVnflcmWithCache() {
        when(hazelcastInstance.getMap(anyString())).thenReturn(vnfInstances);
        vnfInstances.put(INSTANCE_ID, Constants.VNF);
        lcmRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }

    @Test
    public void noVnflcmHostProvided() {
        lcmRouterGatewayFilterFactory.apply(new Config()).filter(exchange, gatewayFilterChain)
                .block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }
}
