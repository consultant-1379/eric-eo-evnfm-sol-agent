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
package com.ericsson.eoevnfmnbi.services.impl;

import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_LEGACY_CVNFM;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.ericsson.eoevnfmnbi.services.PaginatedRequestService;

/**
 * Allows legacy behaviour for CVNFM: nextpage_opaque_marker query parameter as number in codeploy setup.
 * Class should be removed once EO-CM fixes pagination logic. Concerned bug: SM-161048
 */
@Component
public class LegacyPaginatedCvnfmService implements PaginatedRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyPaginatedCvnfmService.class);

    @Override
    public ServerWebExchange performQuery(final ServerWebExchange exchange, final WebClient.Builder webClientBuilder) {
        LOGGER.debug("Performing direct query to CVNFM by url: {}", exchange.getRequest().getURI().getPath());
        return exchange;
    }

    @Override
    public Pattern getNextPageOpaqueMarkerPattern() {
        return NEXTPAGE_OPAQUE_MARKER_LEGACY_CVNFM;
    }
}
