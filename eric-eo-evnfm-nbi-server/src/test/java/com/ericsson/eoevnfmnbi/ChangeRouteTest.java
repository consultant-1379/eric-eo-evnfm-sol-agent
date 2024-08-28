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
package com.ericsson.eoevnfmnbi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import com.ericsson.eoevnfmnbi.utils.FilterUtils;

public class ChangeRouteTest {

    @Test
    public void changeRouteTest() {
        String host = "http://localhost";
        String uri = host + ":8084";
        String uriPrimary = host + ":8888";
        String uriSecondary = host + ":10101";
        String routeId = "Origin Path ID";
        int order = 1;

        MockServerHttpRequest request = MockServerHttpRequest
                .get(uri)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Route routePrimary = Route.async().id(routeId).uri(URI.create(uriPrimary)).order(order)
                .predicate(swe -> true).build();

        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, routePrimary);

        FilterUtils.changeRoute(uriSecondary, exchange);

        Route routeSecondary = Route.async().id(routeId).uri(URI.create(uriSecondary)).order(order)
                .predicate(swe -> true).build();

        Route routeUpdated = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertThat(routeSecondary.getUri()).isEqualTo(routeUpdated.getUri());
    }

    @Test
    public void changeRouteUriNullTest() {
        String host = "http://localhost";
        String uri = host + ":8084";
        String uriPrimary = host + ":8888";
        String invalidUriSecondary = "local^host";
        String routeId = "Origin Path ID";
        int order = 1;

        MockServerHttpRequest request = MockServerHttpRequest
                .get(uri)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Route routePrimary = Route.async().id(routeId).uri(URI.create(uriPrimary)).order(order)
                .predicate(swe -> true).build();

        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, routePrimary);

        FilterUtils.changeRoute(invalidUriSecondary, exchange);

        Route routeUpdated = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assertThat(routePrimary.getUri()).isEqualTo(routeUpdated.getUri());
    }
}