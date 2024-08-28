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
package com.ericsson.eoevnfmnbi.utils;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static com.ericsson.eoevnfmnbi.utils.Constants.PARAMETERIZED_TYPE_REFERENCE;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import javax.net.ssl.SSLHandshakeException;

import org.apache.logging.log4j.util.Strings;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebExchange;

import com.ericsson.eoevnfmnbi.exceptions.ConnectionFailureException;

import io.netty.handler.timeout.TimeoutException;
import reactor.core.publisher.Mono;

public final class FilterUtils {

    private static final Pattern REGEX = Pattern.compile("\\{.*}");
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterUtils.class);

    private FilterUtils() {
        // no constructor
    }

    public static GatewayFilter applyLcmFilterGateway(String urlRoute, BiFunction<ServerWebExchange, GatewayFilterChain, Mono<Void>> changeRoute) {
        return (exchange, chain) -> {
            LOGGER.info("Requested route: {}", exchange.getRequest().getPath());
            exchange.mutate().response(new SecureHeadersResponseDecorator(exchange.getResponse()));
            if (urlRoute != null) {
                LOGGER.debug("Performing routing");
                return changeRoute.apply(exchange, chain);
            }
            LOGGER.debug("Skipping routing");
            return chain.filter(exchange);
        };
    }

    public static Mono<Void> routeRequest(ServerWebExchange exchange, GatewayFilterChain chain, String urlRoute) {
        changeRoute(urlRoute, exchange);
        return chain.filter(exchange);
    }

    public static void changeRoute(String urlRoute, ServerWebExchange exchange) {
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        if (route != null) {
            RouteDefinition routeDefinition = new RouteDefinition();
            routeDefinition.setId(route.getId());
            routeDefinition.setOrder(route.getOrder());
            try {
                routeDefinition.setUri(new URI(urlRoute));
            } catch (URISyntaxException e) {
                LOGGER.error("New route URI is malformed and cannot be used to change route {} ", e.getMessage());
                return;
            }

            Route.AsyncBuilder routeBuilder = Route.async(routeDefinition);
            routeBuilder.asyncPredicate(route.getPredicate());

            Route newRoute = routeBuilder.build();

            LOGGER.info("Applying new route from {}", route);
            exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, newRoute);
            LOGGER.info("Switching route to {}", newRoute);
        }
    }

    public static List<String> getUrlConfigList() {
        return List.of("url");
    }

    public static String getUriVariable(ServerWebExchange exchange, String resourceType) {
        Map<String, String> uriTemplateVariables = ServerWebExchangeUtils.getUriTemplateVariables(exchange);
        String uriVariable = uriTemplateVariables.get(resourceType);
        if (Strings.isEmpty(uriVariable)) {
            return Strings.EMPTY;
        }
        return uriVariable;
    }

    public static int getResourceById(String host, String path, String resourceId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        String url = host + "/" + REGEX.matcher(path).replaceAll(resourceId);
        int responseCode = HttpStatus.NOT_FOUND.value();
        LOGGER.debug("Performing GET VNF instance request: {}", url);
        try {
            return new RestTemplate().exchange(url, HttpMethod.GET, requestEntity, String.class).getStatusCode().value();
        } catch (RestClientException e) { // NOSONAR
            Throwable cause = e.getMostSpecificCause();
            handleGetResourceException(cause, host, resourceId);
            LOGGER.warn("Request to {} for {} failed", host + path, resourceId);
        }
        LOGGER.debug("Response: {}", responseCode);
        return responseCode;
    }

    public static boolean anyItems(String host, String path, String queryParam) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        String url = host + "/" + path + "?" + queryParam;
        LOGGER.debug("Performing  request: {}", url);
        try {
            ResponseEntity<List<Object>> response = new RestTemplate().exchange(url, HttpMethod.GET, requestEntity,
                                                                                PARAMETERIZED_TYPE_REFERENCE);
            return checkResponse(response);
        } catch (RestClientException e) { // NOSONAR
            Throwable cause = e.getMostSpecificCause();
            handleGetResourceException(cause, host);
            LOGGER.warn("Request to {} for failed", host + path);
        }
        return true;
    }

    public static boolean checkResponse(ResponseEntity<List<Object>> response) {
        LOGGER.debug("Response: {}", response);
        if (response.getStatusCode().is2xxSuccessful()) {
            List<Object> body = response.getBody();
            if (body != null) {
                return !body.isEmpty();
            }
            return false;
        }
        return true;
    }

    public static Optional<Mono<Void>> handleErrorStatusCode(ServerHttpResponse response, Publisher<? extends DataBuffer> body) {
        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode == null) {
            LOGGER.error("Http status code is not provided during action. ServerHttpResponse: {}", response);
            return Optional.of(response.writeWith(body));
        } else {
            if (statusCode.isError()) {
                return Optional.of(response.writeWith(body));
            }
        }
        return Optional.empty();
    }

    private static void handleGetResourceException(Throwable e, String host, String resourceId) {
        LOGGER.error("While getting resource with host: [{}], resourceId: [{}]," +
                             " got exception: {}", host, resourceId, e.getMessage(), e);
        String exceptionDetail = e.getMessage();
        if (e instanceof UnknownHostException) {
            exceptionDetail = "Unknown host: " + host;
        }
        if (isConnectionFailureException(e)) {
            throw new ConnectionFailureException(String.format("Cannot get resource with id %s. Details: %s", resourceId, exceptionDetail));
        }
    }

    private static void handleGetResourceException(Throwable e, String host) {
        LOGGER.error("While getting resource with host: [{}], " +
                             " got exception: {}", host, e.getMessage(), e);
        String exceptionDetail = e.getMessage();
        if (e instanceof UnknownHostException) {
            exceptionDetail = "Unknown host: " + host;
        }
        if (isConnectionFailureException(e)) {
            throw new ConnectionFailureException(String.format("Cannot execute get request. Details: %s", exceptionDetail));
        }
    }

    public static boolean isRetryableException(Throwable e) {
        if (e instanceof WebClientRequestException) {
            return e.getCause().getCause() instanceof SocketTimeoutException
                    || e.getCause().getCause() instanceof SocketException
                    || e.getCause().getCause() instanceof TimeoutException;
        }
        return e instanceof SocketTimeoutException
                || e instanceof SocketException
                || e instanceof TimeoutException;
    }

    public static boolean isConnectionFailureException(Throwable e) {
        return isRetryableException(e) || e instanceof UnknownHostException ||
                e.getCause() instanceof UnknownHostException || e instanceof SSLHandshakeException;
    }
}