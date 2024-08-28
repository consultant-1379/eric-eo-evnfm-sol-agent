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
package com.ericsson.eoevnfmnbi.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.netty.resources.ConnectionProvider;

@Configuration
public class CustomConnectionProviderConfig {

    @Value("${connection.provider.maxConnections}")
    private int maxConnections;
    @Value("${connection.provider.maxIdleTime}")
    private long maxIdleTime;
    @Value("${connection.provider.maxLifeTime}")
    private long maxLifeTime;
    @Value("${connection.provider.pendingAcquireTimeout}")
    private long pendingAcquireTimeout;
    @Value("${connection.provider.evictInBackground}")
    private long evictInBackground;

    @Bean
    public ConnectionProvider customConnectionProvider() {
        return ConnectionProvider.builder("custom-provider")
                .maxConnections(maxConnections)
                .maxIdleTime(Duration.ofSeconds(maxIdleTime))
                .maxLifeTime(Duration.ofSeconds(maxLifeTime))
                .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeout))
                .evictInBackground(Duration.ofSeconds(evictInBackground)).build();
    }
}
