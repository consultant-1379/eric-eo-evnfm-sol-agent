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
package com.ericsson.eoevnfmnbi.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

@ContextConfiguration(classes = { DirectRequestService.class })
@ExtendWith({ MockitoExtension.class, SpringExtension.class})
@TestPropertySource(properties = { "hosts.secondary = " })
class DirectRequestServiceTest {

    @Autowired
    private DirectRequestService directRequestService;

    @Mock
    private WebClient.Builder webBuilder;

    @Test
    void performQueryTestStandaloneVNFM() throws URISyntaxException {
        String host = "http://localhost";
        String uri = host + ":8084";
        String uriPrimary = host + ":8888";
        String routeId = "Origin Path ID";
        int order = 1;

        MockServerHttpRequest request = MockServerHttpRequest
                .get(uri)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Route routePrimary = Route.async().id(routeId).uri(URI.create(uriPrimary)).order(order)
                .predicate(swe -> true).build();

        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, routePrimary);

        Route routeUpdated = directRequestService.performQuery(exchange, webBuilder).getAttribute(GATEWAY_ROUTE_ATTR);
        assertThat(routePrimary.getUri()).isEqualTo(routeUpdated.getUri());
    }
}