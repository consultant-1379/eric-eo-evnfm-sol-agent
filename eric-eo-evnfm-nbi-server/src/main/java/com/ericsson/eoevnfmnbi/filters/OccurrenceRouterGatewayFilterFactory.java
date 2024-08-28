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

import static com.ericsson.eoevnfmnbi.utils.Constants.CNF;
import static com.ericsson.eoevnfmnbi.utils.Constants.VNF;
import static com.ericsson.eoevnfmnbi.utils.Constants.VNF_OCCURRENCES;
import static com.ericsson.eoevnfmnbi.utils.Constants.VNF_OCCURRENCE_ID;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.applyLcmFilterGateway;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.changeRoute;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.getResourceById;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.getUriVariable;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.getUrlConfigList;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.ericsson.eoevnfmnbi.config.VnfmConfig;
import com.ericsson.eoevnfmnbi.models.Config;
import com.ericsson.eoevnfmnbi.models.Generic;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import reactor.core.publisher.Mono;

@Component
public class OccurrenceRouterGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OccurrenceRouterGatewayFilterFactory.class);

    @Autowired
    private VnfmConfig vnfmConfig;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    public OccurrenceRouterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return getUrlConfigList();
    }

    @Override
    public GatewayFilter apply(Config config) {
        String urlRoute = config.getUrl();
        return applyLcmFilterGateway(urlRoute, (exchange, chain) -> routeRequest(exchange, chain, urlRoute));
    }

    private Mono<Void> routeRequest(ServerWebExchange exchange, GatewayFilterChain chain, String urlRoute) {
        String occurrenceId = getUriVariable(exchange, VNF_OCCURRENCE_ID);

        return verifyOccurrenceId(exchange, urlRoute, occurrenceId)
                .then(chain.filter(exchange));
    }

    private Mono<Generic> verifyOccurrenceId(ServerWebExchange exchange, String urlRoute, String occurrenceId) {
        IMap<Object, Object> vnfOccurrences = hazelcastInstance.getMap(VNF_OCCURRENCES);
        String id = (String) vnfOccurrences.get(occurrenceId);
        if (id == null) {
            LOGGER.debug("Type is not present in cache for VNF occurrence id: {}", occurrenceId);
            if (getResourceById(vnfmConfig.getHost(), vnfmConfig.getOccurrence().getPath(), occurrenceId) == HttpStatus.OK.value()) {
                LOGGER.debug("Type is CNF for VNF occurrence id: {}", occurrenceId);
                vnfOccurrences.put(occurrenceId, CNF);
            } else {
                LOGGER.debug("Type is VNF for VNF occurrence id: {}", occurrenceId);
                vnfOccurrences.put(occurrenceId, VNF);
                changeRoute(urlRoute, exchange);
            }
            return Mono.empty();
        } else if (id.equalsIgnoreCase(CNF)) {
            LOGGER.debug("Type is {} for VNF occurrence id: {}", id, occurrenceId);
            return Mono.empty();
        } else {
            LOGGER.debug("Type is {} for VNF occurrence id: {}", id, occurrenceId);
            changeRoute(urlRoute, exchange);
            return Mono.empty();
        }
    }
}