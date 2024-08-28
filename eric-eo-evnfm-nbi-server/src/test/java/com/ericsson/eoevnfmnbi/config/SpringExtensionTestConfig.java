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

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.ericsson.eoevnfmnbi.config.retrytemplate.RetryTemplateConfig;

import okhttp3.OkHttpClient;
import reactor.netty.resources.ConnectionProvider;

@TestConfiguration
@Import({OnboardingConfig.class, NfvoConfig.class, VnfmConfig.class, Config.class, RetryTemplateConfig.class})
public class SpringExtensionTestConfig {

    @Bean
    public ConnectionProvider customConnectionProvider() {
        return ConnectionProvider.builder("custom-provider")
            .maxConnections(5)
            .maxIdleTime(Duration.ofSeconds(5))
            .maxLifeTime(Duration.ofSeconds(5))
            .pendingAcquireTimeout(Duration.ofSeconds(10))
            .evictInBackground(Duration.ofSeconds(20)).build();
    }

    @Bean
    public OkHttpClient getclient() {
        return new OkHttpClient();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
