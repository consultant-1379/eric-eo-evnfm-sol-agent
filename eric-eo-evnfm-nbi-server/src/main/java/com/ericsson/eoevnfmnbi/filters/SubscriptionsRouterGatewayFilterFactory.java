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

import static com.ericsson.eoevnfmnbi.utils.FilterUtils.applyLcmFilterGateway;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.getUrlConfigList;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.routeRequest;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import com.ericsson.eoevnfmnbi.models.Config;

@Component
public class SubscriptionsRouterGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    public SubscriptionsRouterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        String urlRoute = config.getUrl();
        return applyLcmFilterGateway(urlRoute, (exchange, chain) -> routeRequest(exchange, chain, urlRoute));
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return getUrlConfigList();
    }
}
