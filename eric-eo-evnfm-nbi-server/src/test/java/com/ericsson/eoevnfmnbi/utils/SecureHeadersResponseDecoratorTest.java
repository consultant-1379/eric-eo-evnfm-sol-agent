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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

class SecureHeadersResponseDecoratorTest {

    @Test
    public void testGetHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.VIA, "HTTP/1.1 GWA");
        httpHeaders.add(HttpHeaders.VIA, "1.1 2e9b3ee4d534903f433e1ed8ea30e57a.cloudfront.net (CloudFront)");
        httpHeaders.add(HttpHeaders.ACCEPT, "text/html");
        ServerHttpResponseDecorator serverHttpResponseDecorator = mock(ServerHttpResponseDecorator.class);
        doReturn(httpHeaders).when(serverHttpResponseDecorator).getHeaders();
        SecureHeadersResponseDecorator secureHeadersResponseDecorator = new SecureHeadersResponseDecorator(serverHttpResponseDecorator);

        HttpHeaders headers = secureHeadersResponseDecorator.getHeaders();

        assertEquals(1, headers.size());
        assertNotNull(headers.get(HttpHeaders.ACCEPT));
    }
}