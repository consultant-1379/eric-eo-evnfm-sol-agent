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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import static utils.CommonUtils.createRoute;
import static utils.CommonUtils.getMockServerHttpRequest;

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
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.eoevnfmnbi.config.SpringExtensionTestConfig;
import com.ericsson.eoevnfmnbi.models.Config;

import reactor.core.publisher.Mono;

@ContextConfiguration(classes = { HazelcastAutoConfiguration.class, SpringExtensionTestConfig.class })
@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class SubscriptionsRouterTest {

    @Spy
    private SubscriptionsRouterGatewayFilterFactory subscriptionsRouterGatewayFilterFactory;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    private static MockServerWebExchange exchange;
    private static Config config;

    private static final String SUBSCRIPTIONS_PATH = "/subscriptions/getnotifications";
    private static final String EVNFM_URI = "http://localhost:8888";
    private static final String VNFLCM_URI = "http://localhost:10101";

    @BeforeEach
    public void setup() {
        MockServerHttpRequest mockServerHttpRequest = getMockServerHttpRequest(SUBSCRIPTIONS_PATH);
        exchange = MockServerWebExchange.from(mockServerHttpRequest);

        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, createRoute("Evnfm", 1, EVNFM_URI));

        when(gatewayFilterChain.filter(Mockito.any())).thenReturn(Mono.empty());
        config = new Config();
    }

    @Test
    public void routedToVnflcmSubscriptions() {
        config.setUrl(VNFLCM_URI);
        subscriptionsRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();

        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(VNFLCM_URI, route.getUri().toString());
    }

    @Test
    public void routedToEvnfmSubscriptions() {
        subscriptionsRouterGatewayFilterFactory.apply(config).filter(exchange, gatewayFilterChain).block();

        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertEquals(EVNFM_URI, route.getUri().toString());
    }
}
