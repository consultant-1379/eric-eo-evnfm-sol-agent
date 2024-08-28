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

import static com.ericsson.eoevnfmnbi.utils.FilterUtils.getUrlConfigList;

import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.ericsson.eoevnfmnbi.models.Config;
import com.ericsson.eoevnfmnbi.services.RequestRouterFactory;
import com.ericsson.eoevnfmnbi.services.RequestService;

import reactor.core.publisher.Mono;

@Component
public class MergeResponseGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeResponseGatewayFilterFactory.class);

    private final WebClient.Builder webClientBuilder;

    @Autowired
    private RequestRouterFactory routerFactory;

    public MergeResponseGatewayFilterFactory(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return getUrlConfigList();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new MergeResponseGatewayFilter(this.webClientBuilder);
    }

    public class MergeResponseGatewayFilter implements GatewayFilter, Ordered {
        private final WebClient.Builder webClientBuilder;

        public MergeResponseGatewayFilter(WebClient.Builder webClientBuilder) {
            this.webClientBuilder = webClientBuilder;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            LOGGER.info("Requested route: {}, filter: MergeResponse", exchange.getRequest().getPath());
            RequestService requestService = routerFactory.getServiceByQueryParams(exchange.getRequest().getQueryParams());
            ServerWebExchange webExchange = exchange;
            try {
                webExchange = requestService.performQuery(exchange, webClientBuilder);
            } catch (URISyntaxException e) {
                LOGGER.error("Unable to route request by path {} due to {}",
                             exchange.getRequest().getPath().value(), e.getMessage());
            }
            if (webExchange.getRequest().getHeaders().getContentType() == null) {
                return chain.filter(webExchange.mutate().request(
                        webExchange.getRequest().mutate()
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build()).build());
            } else {
                return chain.filter(webExchange.mutate().build());
            }
        }

        @Override
        public int getOrder() {
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
        }
    }
}
