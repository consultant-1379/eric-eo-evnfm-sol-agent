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

import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_CVNFM_PREFIX;
import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_FIRST_PAGE_VMVNFM_PREFIX;
import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_VMVNFM_PREFIX;
import static com.ericsson.eoevnfmnbi.utils.Constants.PAGINATION_INFO_HEADER;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.anyItems;
import static com.ericsson.eoevnfmnbi.utils.FilterUtils.handleErrorStatusCode;
import static com.ericsson.eoevnfmnbi.utils.LinksUtils.buildLinks;
import static com.ericsson.eoevnfmnbi.utils.LinksUtils.parseLinks;
import static com.ericsson.eoevnfmnbi.utils.PaginationInfoUtils.getTotalElementsFromResponseHeader;
import static com.ericsson.eoevnfmnbi.utils.PaginationInfoUtils.isLastPage;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CvnfmHttpResponseDecorator extends ServerHttpResponseDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CvnfmHttpResponseDecorator.class);
    private final ServerWebExchange exchange;
    private final String host;

    private final WebClient.Builder webClientBuilder;

    public CvnfmHttpResponseDecorator(ServerWebExchange exchange, String host,
                                      final WebClient.Builder webClientBuilder) {
        super(exchange.getResponse());
        this.exchange = exchange;
        this.host = host;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        Optional<Mono<Void>> statusCodeResult = handleErrorStatusCode(getDelegate(), body);
        if (statusCodeResult.isPresent()) {
            return statusCodeResult.get();
        }

        HttpHeaders headers = getDelegate().getHeaders();
        int totalCnfElements = getTotalElementsFromResponseHeader(headers);
        boolean isLastCnfPage = isLastPage(headers);

        if (totalCnfElements == 0) {
            LOGGER.debug("There are no items in CVNFM, retrieving first page from VM VNFM");
            return returnVmVnfmFirstPage(headers);
        }
        boolean anyItemsInVmVnfm = false;
        if (isLastCnfPage) {
            LOGGER.debug("This is the last page CVNFM, checking whether VM VNFM has any items to set \"next\" link");
            anyItemsInVmVnfm = anyItems(host, exchange.getRequest().getPath().value(), "size=1");
            LOGGER.debug("Check on whether there is at least one item in VM VNFM returned: {}.", anyItemsInVmVnfm);
        }
        if (headers.containsKey(HttpHeaders.LINK)) {
            Map<String, String> links = parseLinks(headers.getFirst(HttpHeaders.LINK));
            String updatedLinksAsString;
            if (isLastCnfPage && anyItemsInVmVnfm) {
                updatedLinksAsString = buildLinks(links, NEXTPAGE_OPAQUE_MARKER_FIRST_PAGE_VMVNFM_PREFIX);
            } else {
                updatedLinksAsString = buildLinks(links, NEXTPAGE_OPAQUE_MARKER_CVNFM_PREFIX);
            }
            LOGGER.debug("Link header to be set: {}", updatedLinksAsString);
            headers.set(HttpHeaders.LINK, updatedLinksAsString);
            headers.remove(PAGINATION_INFO_HEADER);
        }

        return getDelegate().writeWith(body);
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return writeWith(Flux.from(body)
                                 .flatMapSequential(p -> p));
    }

    private Mono<Void> returnVmVnfmFirstPage(HttpHeaders headers) {
        Mono<ResponseEntity<String>> secondResponse = fetchData(host, exchange.getRequest().getPath().value(), exchange.getRequest().getHeaders(),
                                                                exchange.getRequest().getQueryParams());
        return secondResponse.flatMap(webResponse -> {
            HttpHeaders newHeaders = webResponse.getHeaders();
            String newBody = webResponse.getBody();
            headers.clear();
            headers.addAll(newHeaders);
            if (headers.containsKey(HttpHeaders.LINK)) {
                Map<String, String> links = parseLinks(headers.getFirst(HttpHeaders.LINK));
                String updatedLinksAsString = buildLinks(links, NEXTPAGE_OPAQUE_MARKER_VMVNFM_PREFIX);
                LOGGER.debug("Link header to be set: {}", updatedLinksAsString);
                headers.set(HttpHeaders.LINK, updatedLinksAsString);
            }
            DataBufferFactory factory = getDelegate().bufferFactory();
            DataBuffer dataBuffer = factory.wrap(newBody.getBytes(StandardCharsets.UTF_8));

            return getDelegate().writeWith(Mono.just(dataBuffer));
        });
    }

    private Mono<ResponseEntity<String>> fetchData(String host, String path, HttpHeaders httpHeaders,
                                                   MultiValueMap<String, String> queryParams) {
        WebClient client = this.webClientBuilder
                .baseUrl(host)
                .build();
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParams(queryParams).build())
                .headers(headers -> headers.addAll(httpHeaders))
                .retrieve()
                .toEntity(String.class);
    }
}
