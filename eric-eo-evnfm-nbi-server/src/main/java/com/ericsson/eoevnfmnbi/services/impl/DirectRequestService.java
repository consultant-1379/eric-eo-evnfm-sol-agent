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

import static com.ericsson.eoevnfmnbi.utils.Constants.TYPE_PARAM;
import static com.ericsson.eoevnfmnbi.utils.Constants.VNF;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.changeRoute;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.eoevnfmnbi.services.RequestService;

@Component
public class DirectRequestService implements RequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectRequestService.class);

    @Value("${hosts.secondary}")
    private String secondaryHost;

    @Override
    public ServerWebExchange performQuery(ServerWebExchange exchange,
                                          WebClient.Builder webClientBuilder) throws URISyntaxException {
        MultiValueMap<String, String> queryParam = exchange.getRequest().getQueryParams();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(exchange.getRequest().getURI());
        if (StringUtils.isEmpty(secondaryHost)) {
            LOGGER.debug("Standalone VNFM is deployed. Performing direct request to {}", exchange.getRequest().getURI());
            return exchange;
        } else {
            if (VNF.equals(queryParam.getFirst(TYPE_PARAM))) {
                changeRoute(secondaryHost, exchange);
            }
            builder.replaceQueryParam(TYPE_PARAM);
            LOGGER.debug("Type query param is {}. Performing direct request to {}", queryParam.getFirst(TYPE_PARAM), exchange.getRequest().getURI());
            return exchange.mutate().request(exchange.getRequest()
                                                     .mutate().uri(new URI(builder.toUriString())).build()).build();
        }
    }
}
