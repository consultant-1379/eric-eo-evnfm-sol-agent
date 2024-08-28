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

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

import java.util.Set;

public class SecureHeadersResponseDecorator extends ServerHttpResponseDecorator {
    private static final Set<String> EXCLUDE_HEADERS = Set.of("Via");

    public SecureHeadersResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = super.getHeaders();
        EXCLUDE_HEADERS.forEach(headers::remove);
        return headers;
    }
}
