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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static com.ericsson.eoevnfmnbi.utils.FilterUtils.checkResponse;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.handleErrorStatusCode;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.isConnectionFailureException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import javax.net.ssl.SSLHandshakeException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import reactor.core.publisher.Mono;

class FilterUtilsTest {

    @Test
    public void testCheckResponseWithNotEmptyBody() {
        List<Object> body = List.of(new Object());
        ResponseEntity<List<Object>> response = new ResponseEntity<>(body, HttpStatusCode.valueOf(200));
        assertTrue(checkResponse(response));
    }

    @Test
    public void testCheckResponseWithNullBody() {
        ResponseEntity<List<Object>> response = new ResponseEntity<>(null, HttpStatusCode.valueOf(200));
        assertFalse(checkResponse(response));
    }

    @Test
    public void testCheckResponseWithEmptyBody() {
        ResponseEntity<List<Object>> response = new ResponseEntity<>(List.of(), HttpStatusCode.valueOf(200));
        assertFalse(checkResponse(response));
    }

    @Test
    public void testCheckResponseWithErrorCode() {
        ResponseEntity<List<Object>> response = new ResponseEntity<>(List.of(), HttpStatusCode.valueOf(400));
        assertTrue(checkResponse(response));
    }

    @Test
    public void testHandleStatusCodeWithNullStatusCode() {
        // Create a mock DataBuffer
        DataBuffer dataBuffer = Mockito.mock(DataBuffer.class);
        Publisher<? extends DataBuffer> body = Mono.just(dataBuffer);

        ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
        when(response.getStatusCode()).thenReturn(null);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        Optional<Mono<Void>> result = handleErrorStatusCode(response, body);

        assertTrue(result.isPresent());
    }

    @Test
    public void testHandleStatusCodeWithErrorStatusCode() {
        // Create a mock DataBuffer
        DataBuffer dataBuffer = Mockito.mock(DataBuffer.class);
        Publisher<? extends DataBuffer> body = Mono.just(dataBuffer);

        ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
        HttpStatusCode statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        when(response.getStatusCode()).thenReturn(statusCode);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        Optional<Mono<Void>> result = handleErrorStatusCode(response, body);

        assertTrue(result.isPresent());
    }

    @Test
    public void testHandleStatusCodeWithSuccessStatusCode() {
        // Create a mock DataBuffer
        DataBuffer dataBuffer = Mockito.mock(DataBuffer.class);
        Publisher<? extends DataBuffer> body = Mono.just(dataBuffer);

        ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
        HttpStatusCode statusCode = HttpStatus.OK;
        when(response.getStatusCode()).thenReturn(statusCode);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        Optional<Mono<Void>> result = handleErrorStatusCode(response, body);

        assertFalse(result.isPresent());
    }

    @Test
    public void testIsConnectionFailureException() {
        Throwable throwable = new RuntimeException();
        assertFalse(isConnectionFailureException(throwable));

        throwable = new UnknownHostException();
        assertTrue(isConnectionFailureException(throwable));

        throwable = new WebClientRequestException(new UnknownHostException(), HttpMethod.GET, URI.create("test"), HttpHeaders.EMPTY);
        assertTrue(isConnectionFailureException(throwable));

        throwable = new WebClientRequestException(new SocketTimeoutException().initCause(new SocketTimeoutException()),
                                                  HttpMethod.GET, URI.create("test"), HttpHeaders.EMPTY);
        assertTrue(isConnectionFailureException(throwable));

        throwable = new WebClientRequestException(new SocketException().initCause(new SocketException()),
                                                  HttpMethod.GET, URI.create("test"), HttpHeaders.EMPTY);
        assertTrue(isConnectionFailureException(throwable));

        throwable = new SocketTimeoutException();
        assertTrue(isConnectionFailureException(throwable));

        throwable = new SocketException();
        assertTrue(isConnectionFailureException(throwable));

        throwable = new SSLHandshakeException("test");
        assertTrue(isConnectionFailureException(throwable));
    }
}