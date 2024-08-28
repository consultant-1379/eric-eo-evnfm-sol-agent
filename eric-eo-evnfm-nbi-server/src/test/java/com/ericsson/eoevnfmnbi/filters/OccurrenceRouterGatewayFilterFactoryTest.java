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
package com.ericsson.eoevnfmnbi.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

import static com.ericsson.eoevnfmnbi.utils.Constants.VNF_OCCURRENCE_ID;

import static utils.CommonUtils.EVNFM_URI;
import static utils.CommonUtils.OCCURRENCE_PATH;
import static utils.CommonUtils.VNFLCM_URI;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.eoevnfmnbi.config.SpringExtensionTestConfig;
import com.ericsson.eoevnfmnbi.config.VnfmConfig;
import com.ericsson.eoevnfmnbi.models.Config;
import com.ericsson.eoevnfmnbi.utils.Constants;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = { HazelcastAutoConfiguration.class, SpringExtensionTestConfig.class })
@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class OccurrenceRouterGatewayFilterFactoryTest {

    private static MockServerWebExchange exchange;
    private static MockWebServer mockWebServer;

    private static Config config = new Config();
    private static final String OCCURRENCE_ID = "l08fcbc8-474f-4673-91ee-761fd833201";
    private static String vnfmHost;

    private IMap<Object, Object> occurrences;
    private static final Map<String, String> URI_TEMPLATE_VARIABLES = new HashMap<>();

    @Spy
    private OccurrenceRouterGatewayFilterFactory occurrenceRouterGatewayFilterFactory;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    @Mock
    private HazelcastInstance hazelcastInstance;

    @BeforeAll
    public static void mockServerSetup() {
        mockWebServer = new MockWebServer();
        vnfmHost = String.valueOf(mockWebServer.url("/"));

        // TODO find a way to extract segment inthe mock httprequest
        URI_TEMPLATE_VARIABLES.put(VNF_OCCURRENCE_ID, OCCURRENCE_ID);
        config.setUrl(VNFLCM_URI);
    }

    @BeforeEach
    public void setup() {
        MockServerHttpRequest mockServerHttpRequest = getMockServerHttpRequest(OCCURRENCE_PATH, OCCURRENCE_ID);
        exchange = MockServerWebExchange.from(mockServerHttpRequest);
        exchange.getAttributes().put(URI_TEMPLATE_VARIABLES_ATTRIBUTE, URI_TEMPLATE_VARIABLES);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, createRoute("Evnfm", 1, EVNFM_URI));

        VnfmConfig vnfmConfig = createVnfmConfig(vnfmHost);

        ReflectionTestUtils.setField(occurrenceRouterGatewayFilterFactory, "vnfmConfig", vnfmConfig);
        ReflectionTestUtils.setField(occurrenceRouterGatewayFilterFactory, "hazelcastInstance", hazelcastInstance);

        Set<HazelcastInstance> allHazelcastInstances = Hazelcast.getAllHazelcastInstances();
        HazelcastInstance realInstance = allHazelcastInstances.stream().findFirst().get();
        occurrences = realInstance.getMap(Constants.VNF_OCCURRENCES);

        when(gatewayFilterChain.filter(Mockito.any())).thenReturn(Mono.empty());
    }

    @AfterEach
    public void eachTestCleanUp() {
        occurrences.evictAll();
    }

    @Test
    public void routedToEvnfmWithoutCache() {
        when(hazelcastInstance.getMap(anyString())).thenReturn(occurrences);
        mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "{}"));
        occurrenceRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }

    @Test
    public void routeToVnflcmWithoutCache() {
        when(hazelcastInstance.getMap(anyString())).thenReturn(occurrences);
        mockWebServer.enqueue(createMockResponse(HttpStatus.NOT_FOUND, "{}"));

        occurrenceRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }

    @Test
    public void routedToEvnfmWithCache() {
        when(hazelcastInstance.getMap(anyString())).thenReturn(occurrences);
        occurrences.put(OCCURRENCE_ID, Constants.CNF);
        occurrenceRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }

    @Test
    public void routedToVnflcmWithCache() {
        when(hazelcastInstance.getMap(anyString())).thenReturn(occurrences);
        occurrences.put(OCCURRENCE_ID, Constants.VNF);
        occurrenceRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }

    @Test
    public void noVnflcmHostProvided() {
        occurrenceRouterGatewayFilterFactory.apply(new Config()).filter(exchange, gatewayFilterChain)
            .block();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }
}
