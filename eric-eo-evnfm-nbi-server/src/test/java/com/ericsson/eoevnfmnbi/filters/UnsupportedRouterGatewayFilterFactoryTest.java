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
import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

import static com.ericsson.eoevnfmnbi.utils.Constants.VNF_INSTANCE_ID;

import static utils.CommonUtils.EVNFM_URI;
import static utils.CommonUtils.UNSUPPORTED_PATH;
import static utils.CommonUtils.VNFLCM_URI;
import static utils.CommonUtils.createMockResponse;
import static utils.CommonUtils.createRoute;
import static utils.CommonUtils.getMockServerHttpRequest;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.eoevnfmnbi.config.SpringExtensionTestConfig;
import com.ericsson.eoevnfmnbi.models.Config;

import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = { SpringExtensionTestConfig.class })
@ExtendWith({ MockitoExtension.class, SpringExtension.class})
class UnsupportedRouterGatewayFilterFactoryTest {
    private static MockServerWebExchange exchange;
    private static MockWebServer mockWebServer;

    private static Config config;
    private static final String INSTANCE_ID = "l08fcbc8-474f-4673-91ee-761fd833201";
    private static String vnfmHost;

    private static final Map<String, String> URI_TEMPLATE_VARIABLES = new HashMap<>();

    @Spy
    private UnsupportedRouterGatewayFilterFactory unsupportedRouterGatewayFilterFactory;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    @BeforeAll
    public static void mockServerSetup() {
        mockWebServer = new MockWebServer();
        vnfmHost = String.valueOf(mockWebServer.url("/"));

        // TODO find a way to extract segment inthe mock httprequest
        URI_TEMPLATE_VARIABLES.put(VNF_INSTANCE_ID, INSTANCE_ID);
    }

    @BeforeEach
    public void setup() {
        config = new Config();
        MockServerHttpRequest mockServerHttpRequest = getMockServerHttpRequest(UNSUPPORTED_PATH, VNF_INSTANCE_ID);
        exchange = MockServerWebExchange.from(mockServerHttpRequest);
        exchange.getAttributes().put(URI_TEMPLATE_VARIABLES_ATTRIBUTE, URI_TEMPLATE_VARIABLES);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, createRoute("Evnfm", 1, EVNFM_URI));

        when(gatewayFilterChain.filter(Mockito.any())).thenReturn(Mono.empty());
    }

    @Test
    public void withoutRerouteToVmVnfm() {
        mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "{}"));
        unsupportedRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }

    @Test
    public void withRerouteToVmVnfm() {
        config.setUrl(VNFLCM_URI);
        mockWebServer.enqueue(createMockResponse(HttpStatus.OK, "{}"));
        unsupportedRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }
}