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
import static com.ericsson.eoevnfmnbi.utils.Constants.VNF_DESCRIPTOR;
import static com.ericsson.eoevnfmnbi.utils.Constants.VNF_INSTANCES;
import static com.ericsson.eoevnfmnbi.utils.Constants.VNF_INSTANCE_ID;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.applyLcmFilterGateway;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.changeRoute;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.getResourceById;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.getUriVariable;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.getUrlConfigList;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.isConnectionFailureException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.eoevnfmnbi.config.NfvoConfig;
import com.ericsson.eoevnfmnbi.config.OnboardingConfig;
import com.ericsson.eoevnfmnbi.config.VnfmConfig;
import com.ericsson.eoevnfmnbi.config.retrytemplate.RetryTemplateConfig;
import com.ericsson.eoevnfmnbi.exceptions.ConnectionFailureException;
import com.ericsson.eoevnfmnbi.models.Config;
import com.ericsson.eoevnfmnbi.models.Generic;
import com.ericsson.eoevnfmnbi.security.CustomX509TrustManager;
import com.ericsson.eoevnfmnbi.utils.FilterUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

@Component
public class LcmRouterGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LcmRouterGatewayFilterFactory.class);

    @Autowired
    private OnboardingConfig onboardingConfig;

    @Autowired
    private VnfmConfig vnfmConfig;

    @Autowired
    private NfvoConfig nfvoConfig;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private CustomX509TrustManager trustManager;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private RetryTemplateConfig retryTemplateConfig;

    @Autowired
    private ConnectionProvider customConnectionProvider;

    @Value("${retry.maxAttempts}")
    private int maxAttempts;

    @Value("${retry.backoff}")
    private long backoff;

    public LcmRouterGatewayFilterFactory() {
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
        String vnfInstanceId = getUriVariable(exchange, VNF_INSTANCE_ID);

        if (Strings.isEmpty(vnfInstanceId)) {
            LOGGER.debug("VNF instance id is not present in URI. Retrieving VNFD id from request body to query package information");
            Flux<DataBuffer> body = exchange.getRequest().getBody();
            return DataBufferUtils.join(body)
                    .map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        return bytes;
                    })
                    .defaultIfEmpty(new byte[0])
                    .flatMap(bytes -> sendRequestFromMessageBody(exchange, chain, urlRoute, bytes));
        } else {
            LOGGER.debug("VNF instance id is: {}", vnfInstanceId);
            return verifyVnfInstance(exchange, urlRoute, vnfInstanceId)
                    .then(chain.filter(exchange));
        }
    }

    private Mono<? extends Void> sendRequestFromMessageBody(ServerWebExchange exchange, GatewayFilterChain chain,
                                                            String urlRoute,
                                                            byte[] bytes) {

        Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
            DataBuffer buffer =
                    exchange.getResponse().bufferFactory().wrap(bytes);
            DataBufferUtils.retain(buffer);
            return Mono.just(buffer);
        });

        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                return cachedFlux;
            }
        };

        ServerWebExchange mutatedExchange =
                exchange.mutate().request(mutatedRequest).build();

        String data = new String(bytes, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        Optional<String> vnfdId = Optional.empty();
        try {
            Generic vnfDescriptorId = objectMapper.readValue(data, Generic.class);
            vnfdId = Optional.ofNullable(vnfDescriptorId.getVnfdId());
        } catch (IOException e) {
            LOGGER.error("Failed to extract VNF descriptor Id: {}", e.getMessage());
        }

        return vnfdId.map(s -> verifyVnfdId(exchange, urlRoute, s)
                .then(chain.filter(mutatedExchange))).orElseGet(() -> chain.filter(mutatedExchange));
    }

    private Mono<List<Generic>> verifyVnfdId(ServerWebExchange exchange, String urlRoute, String vnfdId) {
        IMap<Object, Object> vnfDescriptors = hazelcastInstance.getMap(VNF_DESCRIPTOR);
        String type = (String) vnfDescriptors.get(vnfdId);
        if (type == null) {
            LOGGER.debug("VNF type is empty for VNFD id: {}, retrieving package information", vnfdId);
            return this.getPackage(vnfdId)
                    .doOnNext(item -> {
                        Generic aPackage = item.get(0);
                        String appDescriptorId = aPackage.getVnfdId();
                        LOGGER.info("vnfdId and appDescriptorId: {}, {}", vnfdId, appDescriptorId);
                        if (!vnfdId.equalsIgnoreCase(appDescriptorId)) {
                            changeRoute(urlRoute, exchange);
                            vnfDescriptors.put(vnfdId, VNF);
                        } else {
                            vnfDescriptors.put(vnfdId, CNF);
                        }
                    })
                    .onErrorResume(error -> {
                        handleGetPackageException(error);
                        vnfDescriptors.put(vnfdId, VNF);
                        changeRoute(urlRoute, exchange);
                        LOGGER.debug("Could not get onboarded package data {}", error.getMessage());
                        return Mono.empty();
                    });
        } else if (type.equalsIgnoreCase(CNF)) {
            LOGGER.debug("Type is {} for VNFD id: {}", type, vnfdId);
            return Mono.empty();
        } else {
            LOGGER.debug("Type is {} for VNFD id: {}", type, vnfdId);
            changeRoute(urlRoute, exchange);
            return Mono.empty();
        }
    }

    private Mono<List<Generic>> getPackage(String vnfdId) {
        if (nfvoConfig.isEnabled()) {
            // When NFVO is enabled it is necessary to use a Http client other than the default RestTemplate, if the default
            // RestTemplate is used then the rest call to the ECM Onboarding service will fail the authentication.
            LOGGER.debug("Retrieving package information from NFVO for VNFD id: {}", vnfdId);
            return getPackageNFVO(vnfdId);
        } else {
            LOGGER.debug("Retrieving package information from EVNFM for VNFD id: {}", vnfdId);
            return getPackageEVNFM(vnfdId);
        }
    }

    public Mono<List<Generic>> getPackageNFVO(final String vnfdId) {
        final URI packageQueryUri = buildUriForQueryOnboardingPackages(vnfdId);
        String responseBody = nfvoHttpClient(packageQueryUri, MediaType.APPLICATION_JSON_VALUE);
        LOGGER.info("PackageInfo: {}", responseBody);
        return parseResponse(responseBody);
    }

    private URI buildUriForQueryOnboardingPackages(String vnfIdentifier) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(onboardingConfig.getHost()).path(onboardingConfig.getPath());
        String query = String.format(onboardingConfig.getQueryValue(), vnfIdentifier);
        return uriComponentsBuilder.query(query).build().toUri();
    }

    private String nfvoHttpClient(URI uri, String acceptHeaderValue) {
        String credentials = Credentials.basic(nfvoConfig.getUsername(), nfvoConfig.getPassword());
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(uri.toURL())
                    .get()
                    .addHeader(HttpHeaders.ACCEPT, acceptHeaderValue)
                    .addHeader("cache-control", "no-cache")
                    .addHeader(HttpHeaders.AUTHORIZATION, credentials)
                    .addHeader("tenantId", nfvoConfig.getTenantId())
                    .build();
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to set URL with reason {}", e.getMessage());
        }
        Response response;
        String responseBody = null;
        final Request finalRequest = request;
        try {
            OkHttpClient client = updateSslContext();
            response = retryTemplateConfig.retryTemplate().execute(context -> client.newCall(finalRequest).execute());
            LOGGER.debug("NfvoHttpClient response: {}", response);
            responseBody = response.body().string();
            LOGGER.debug("NfvoHttpClient body: {}", responseBody);
        } catch (Exception e) {
            handleGetPackageException(e);
            LOGGER.error("Failed to retrieve expected response for URI {} due to reason:: {}", uri, e.getMessage());
        }
        return responseBody;
    }

    public Mono<List<Generic>> parseResponse(final String responseBody) {
        Generic[] packageResponses = new Generic[0];
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            packageResponses = objectMapper.readValue(responseBody, Generic[].class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            LOGGER.error("Failed to parse response due to reason:: {}", e.getMessage());
            return Mono.empty();
        }
        return Mono.just(Arrays.asList(packageResponses));
    }

    public OkHttpClient updateSslContext() {
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {trustManager}, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            LOGGER.error("Failed to set SSL context with reason {}", e.getMessage());
        }
        return this.okHttpClient.newBuilder()
                .sslSocketFactory(Objects.requireNonNull(sslSocketFactory), trustManager)
                .build();
    }

    public Mono<List<Generic>> getPackageEVNFM(String vnfIdentifier) {
        WebClient client = WebClient.builder()
                .baseUrl(onboardingConfig.getHost())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(customConnectionProvider)))
                .build();
        return client.get()
                .uri(uriBuilder -> {
                    String query = String.format(onboardingConfig.getQueryValue(), vnfIdentifier);
                    return uriBuilder.path(onboardingConfig.getPath()).queryParam("filter", query).build();
                })
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(resp -> LOGGER.info("PackageInfo: {}", resp))
                .flatMap(this::parseResponse)
                .flatMapMany(Flux::fromIterable)
                .retryWhen(Retry.fixedDelay(maxAttempts, Duration.ofMillis(backoff)).filter(FilterUtils::isRetryableException))
                .collectList();
    }

    private Mono<Generic> verifyVnfInstance(ServerWebExchange exchange, String urlRoute, String vnfInstanceId) {
        IMap<Object, Object> vnfInstances = hazelcastInstance.getMap(VNF_INSTANCES);
        String type = (String) vnfInstances.get(vnfInstanceId);
        if (type == null) {
            LOGGER.debug("Type is not present in cache for VNF instance id: {}. Retrieving VNF instance from CVNFM", vnfInstanceId);
            if (getResourceById(vnfmConfig.getHost(), vnfmConfig.getVnf().getPath(), vnfInstanceId) == HttpStatus.OK.value()) {
                LOGGER.debug("Type is CNF for VNF instance id: {}", vnfInstanceId);
                vnfInstances.put(vnfInstanceId, CNF);
            } else {
                LOGGER.debug("Type is VNF for VNF instance id: {}", vnfInstanceId);
                vnfInstances.put(vnfInstanceId, VNF);
                changeRoute(urlRoute, exchange);
            }
            return Mono.empty();
        } else if (type.equalsIgnoreCase(CNF)) {
            LOGGER.debug("Type is {} for VNF instance id: {}", type, vnfInstanceId);
            return Mono.empty();
        } else {
            LOGGER.debug("Type is {} for VNF instance id: {}", type, vnfInstanceId);
            changeRoute(urlRoute, exchange);
            return Mono.empty();
        }
    }

    private void handleGetPackageException(Throwable e) {
        LOGGER.error("While getting package got exception: {}", e.getMessage(), e);
        Throwable exception = Exceptions.isRetryExhausted(e) ? e.getCause() : e;
        String exceptionDetails = exception.getMessage();
        if (exception instanceof UnknownHostException) {
            exceptionDetails = "Unknown host: " + onboardingConfig.getHost();
        }
        if (isConnectionFailureException(exception)) {
            throw new ConnectionFailureException(String.format("Cannot get package details. Details: %s", exceptionDetails));
        }
    }
}
