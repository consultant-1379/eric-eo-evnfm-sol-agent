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

import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_CVNFM_PATTERN;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.eoevnfmnbi.decorators.CvnfmHttpResponseDecorator;
import com.ericsson.eoevnfmnbi.services.PaginatedRequestService;

@Component
public class PaginatedPageCvnfmService implements PaginatedRequestService {

    @Value("${hosts.secondary}")
    private String secondaryVnfmHost;

    @Override
    public ServerWebExchange performQuery(ServerWebExchange exchange,
                                          WebClient.Builder webClientBuilder) throws URISyntaxException {
        MultiValueMap<String, String> queryMap = exchange.getRequest().getQueryParams();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(exchange.getRequest().getURI());
        setPageSize(queryMap, builder);
        setNextPageOpaqueMarker(queryMap, builder);
        ServerWebExchange webExchange = exchange.mutate().request(exchange.getRequest()
                                                                          .mutate().uri(new URI(builder.toUriString())).build()).build();
        ServerHttpResponseDecorator responseDecorator = new CvnfmHttpResponseDecorator(webExchange, secondaryVnfmHost,
                                                                                       webClientBuilder);

        return webExchange.mutate().response(responseDecorator).build();
    }

    @Override
    public Pattern getNextPageOpaqueMarkerPattern() {
        return NEXTPAGE_OPAQUE_MARKER_CVNFM_PATTERN;
    }
}
