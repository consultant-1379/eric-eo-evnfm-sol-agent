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
package com.ericsson.eoevnfmnbi.decorators;

import static com.ericsson.eoevnfmnbi.utils.Constants.PAGINATION_INFO_HEADER;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.handleErrorStatusCode;
import static com.ericsson.eoevnfmnbi.utils.LinksUtils.buildLinks;
import static com.ericsson.eoevnfmnbi.utils.LinksUtils.parseLinks;

import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

import reactor.core.publisher.Mono;

public class VmVnfmHttpResponseDecorator extends ServerHttpResponseDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmVnfmHttpResponseDecorator.class);

    private final String paginationPrefix;

    public VmVnfmHttpResponseDecorator(final ServerHttpResponse delegate, String paginationPrefix) {
        super(delegate);
        this.paginationPrefix = paginationPrefix;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        Optional<Mono<Void>> statusCodeResult = handleErrorStatusCode(getDelegate(), body);
        if (statusCodeResult.isPresent()) {
            return statusCodeResult.get();
        }

        HttpHeaders headers = getDelegate().getHeaders();
        if (headers.containsKey(HttpHeaders.LINK)) {
            Map<String, String> links = parseLinks(headers.getFirst(HttpHeaders.LINK));
            String updatedLinksAsString = buildLinks(links, paginationPrefix);
            LOGGER.debug("Link header to be set: {}", updatedLinksAsString);
            headers.set(HttpHeaders.LINK, updatedLinksAsString);
            headers.remove(PAGINATION_INFO_HEADER);
        }

        return getDelegate().writeWith(body);
    }
}
