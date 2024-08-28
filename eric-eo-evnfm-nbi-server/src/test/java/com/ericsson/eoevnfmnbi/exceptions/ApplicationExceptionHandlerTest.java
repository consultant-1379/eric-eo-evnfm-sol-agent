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
package com.ericsson.eoevnfmnbi.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import static com.ericsson.eoevnfmnbi.utils.Constants.GATEWAY_TIMEOUT_EXCEPTION_MESSAGE;
import static com.ericsson.eoevnfmnbi.utils.Constants.STATUS_RESPONSE_PARAM_KEY;
import static com.ericsson.eoevnfmnbi.utils.Constants.TIMEOUT_ERROR_RESPONSE_PARAMS;

import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.ericsson.eoevnfmnbi.config.ExceptionHandlerConfig;
import com.ericsson.eoevnfmnbi.config.TestConfig;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {ExceptionHandlerConfig.class})
@Import(TestConfig.class)
public class ApplicationExceptionHandlerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationExceptionHandler exceptionHandler;

    @Test
    void shouldReturnTimeoutErrorAttributes() {
        Throwable error = new RuntimeException(GATEWAY_TIMEOUT_EXCEPTION_MESSAGE);
        String host = "http://localhost";
        String uri = host + ":8084";

        ServerRequest serverRequest = MockServerRequest.builder()
                .uri(URI.create(uri))
                .attribute("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", error)
                .build();

        Map<String, Object> errorAttributes = exceptionHandler.getErrorAttributes(serverRequest, ErrorAttributeOptions.defaults());

        assertThat(errorAttributes).isEqualTo(TIMEOUT_ERROR_RESPONSE_PARAMS);
    }

    @Test
    void shouldReturnDefaultCustomExceptionErrorAttributes() {
        Throwable error = new DefaultCustomException("Test Exception");
        String host = "http://localhost";
        String uri = host + ":8084";

        ServerRequest serverRequest = MockServerRequest.builder()
                .uri(URI.create(uri))
                .attribute("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", error)
                .build();

        Map<String, Object> errorAttributes = exceptionHandler.getErrorAttributes(serverRequest, ErrorAttributeOptions.defaults());

        assertThat(errorAttributes.get("detail")).isEqualTo("Test Exception");
        assertThat(errorAttributes.get("title")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(errorAttributes.get("type")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(errorAttributes.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }


    @Test
    void shouldReturnDefaultCustomExceptionErrorAttributesWhenNotGatewayTimeoutException() {
        Throwable error = new RuntimeException("Test Exception");
        String host = "http://localhost";
        String uri = host + ":8084";

        ServerRequest serverRequest = MockServerRequest.builder()
                .uri(URI.create(uri))
                .attribute("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", error)
                .build();

        Map<String, Object> errorAttributes = exceptionHandler.getErrorAttributes(serverRequest, ErrorAttributeOptions.defaults());

        assertThat(errorAttributes.get("detail")).isEqualTo("Test Exception");
        assertThat(errorAttributes.get("title")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(errorAttributes.get("type")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(errorAttributes.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void getHttpStatusFromNotEmptyErrorAttributes() {
        Map<String, Object> errorAttributes = Map.of(STATUS_RESPONSE_PARAM_KEY, 202);

        int httpStatus = exceptionHandler.getHttpStatus(errorAttributes);

        assertThat(httpStatus).isEqualTo(202);
    }

    @Test
    void getHttpStatusFromEmptyErrorAttributes() {
        Map<String, Object> errorAttributes = new HashMap<>();

        int httpStatus = exceptionHandler.getHttpStatus(errorAttributes);

        assertThat(httpStatus).isEqualTo(INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldReturnConnectException() {
        Throwable error = new ConnectException("Test Exception");
        String host = "http://localhost";
        String uri = host + ":8084";

        MockServerHttpRequest httpRequest = MockServerHttpRequest.get(uri).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);

        ServerRequest serverRequest = MockServerRequest.builder()
                .uri(URI.create(uri))
                .attribute("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", error)
                .attribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl", "VM-VNFM_HOST")
                .exchange(exchange)
                .build();

        Mono<ServerResponse> errorAttributes = exceptionHandler.renderErrorResponse(serverRequest);
        ServerResponse serverResponse = errorAttributes.block();

        assertThat(serverResponse.statusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldReturnCvnfmGatewayTimeoutException() {
        Throwable error = new RuntimeException(GATEWAY_TIMEOUT_EXCEPTION_MESSAGE);
        String host = "http://localhost";
        String uri = host + ":8084";

        MockServerHttpRequest httpRequest = MockServerHttpRequest.get(uri).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);

        ServerRequest serverRequest = MockServerRequest.builder()
                .uri(URI.create(uri))
                .attribute("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", error)
                .attribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl", "http://eric-vnfm-orchestrator-service:8888")
                .exchange(exchange)
                .build();

        Mono<ServerResponse> errorAttributes = exceptionHandler.renderErrorResponse(serverRequest);
        ServerResponse serverResponse = errorAttributes.block();

        assertThat(serverResponse.statusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    void shouldReturnOnboardingGatewayTimeoutException() {
        Throwable error = new RuntimeException(GATEWAY_TIMEOUT_EXCEPTION_MESSAGE);
        String host = "http://localhost";
        String uri = host + ":8084";

        MockServerHttpRequest httpRequest = MockServerHttpRequest.get(uri).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);

        ServerRequest serverRequest = MockServerRequest.builder()
                .uri(URI.create(uri))
                .attribute("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", error)
                .attribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl", "http://eric-am-onboarding-service:8888")
                .exchange(exchange)
                .build();

        Mono<ServerResponse> errorAttributes = exceptionHandler.renderErrorResponse(serverRequest);
        ServerResponse serverResponse = errorAttributes.block();

        assertThat(serverResponse.statusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    void shouldReturnWebClientResponseExceptionException() {
        Throwable error = new WebClientResponseException(INTERNAL_SERVER_ERROR.value(), "server error", HttpHeaders.EMPTY, new byte[1],
                                                         StandardCharsets.UTF_8);
        String host = "http://localhost";
        String uri = host + ":8084";

        MockServerHttpRequest httpRequest = MockServerHttpRequest.get(uri).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);

        ServerRequest serverRequest = MockServerRequest.builder()
                .uri(URI.create(uri))
                .attribute("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", error)
                .attribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl", "http://eric-am-onboarding-service:8888")
                .exchange(exchange)
                .build();

        Mono<ServerResponse> errorAttributes = exceptionHandler.renderErrorResponse(serverRequest);
        ServerResponse serverResponse = errorAttributes.block();

        assertThat(serverResponse.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}