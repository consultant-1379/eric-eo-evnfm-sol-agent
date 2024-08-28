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
package com.ericsson.eoevnfmnbi.services;

import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_CVNFM_PATTERN;
import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_PARAM;
import static com.ericsson.eoevnfmnbi.utils.Constants.TYPE_PARAM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.ericsson.eoevnfmnbi.exceptions.DefaultCustomException;
import com.ericsson.eoevnfmnbi.services.impl.DirectRequestService;

/**
 * RequestRouterFactory class decides how request will be handled. Possible scenarios listed according
 * to priority level:
 * <p>
 * 1. If standalone application is deployed (hosts.secondary is null), no pagination is applied,
 * call is executed directly towards service. {@link DirectRequestService}
 * 2. If "type" query parameter is specified with value: VNF or CNF, no pagination is applied,
 * call is executed directly towards service. {@link DirectRequestService}
 * 3. If "nextpage_opaque_marker" query parameter is absent, call towards CVNFM will be performed
 * to retrieve the first page. {@link com.ericsson.eoevnfmnbi.services.impl.PaginatedPageCvnfmService}
 * 4. If "nextpage_opaque_marker" query parameter contains CVNFM token (e.g. cvnfm-1), call towards
 * CVNFM will be performed to retrieve the page. {@link com.ericsson.eoevnfmnbi.services.impl.PaginatedPageCvnfmService}
 * 5. If "nextpage_opaque_marker" query parameter contains VM VNFM token (e.g. vmvnfm-1), call towards
 * VM VNFM will be performed to retrieve the page. {@link com.ericsson.eoevnfmnbi.services.impl.PaginatedPageVmVnfmService}
 * 6. If "nextpage_opaque_marker" query parameter contains VM VNFM token but without any value (e.g. vmvnfm),
 * call towards VM VNFM will be performed to retrieve the first page. This means that there are no items
 * left in CVNFM and items iteration will go to VM VNFM. {@link com.ericsson.eoevnfmnbi.services.impl.PaginatedPageVmVnfmService}
 **/
@Component
public class RequestRouterFactory {

    private static final String LOG_ROUTER = "Request router: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRouterFactory.class);

    private final Map<Pattern, PaginatedRequestService> paginatedRequestServiceCache = new HashMap<>();

    @Value("${hosts.secondary}")
    private String secondaryHost;

    @Autowired
    private List<PaginatedRequestService> paginatedRequestServiceList;

    @Autowired
    private DirectRequestService directRequestService;

    @PostConstruct
    public void initServiceCache() {
        for (PaginatedRequestService paginatedRequestService : paginatedRequestServiceList) {
            paginatedRequestServiceCache.put(paginatedRequestService.getNextPageOpaqueMarkerPattern(), paginatedRequestService);
        }
    }

    public RequestService getServiceByQueryParams(MultiValueMap<String, String> queryParams) {
        if (StringUtils.isEmpty(secondaryHost) || queryParams.containsKey(TYPE_PARAM)) {
            LOGGER.info(LOG_ROUTER, directRequestService.getClass());
            return directRequestService;
        }
        if (queryParams.containsKey(NEXTPAGE_OPAQUE_MARKER_PARAM)) {
            String opaqueMarker = queryParams.getFirst(NEXTPAGE_OPAQUE_MARKER_PARAM);
            for (PaginatedRequestService paginatedRequestService : paginatedRequestServiceList) {
                if (paginatedRequestService.getNextPageOpaqueMarkerPattern().matcher(opaqueMarker).matches()) {
                    LOGGER.info(LOG_ROUTER, paginatedRequestService.getClass());
                    return paginatedRequestService;
                }
            }
            throw new DefaultCustomException("Wrong query parameter",
                                             String.format("Invalid page value for nextpage_opaque_marker:: \"%s\". Parameter must specify "
                                                                   + "either CVNFM token e.g. "
                                                                   + "\"nextpage_opaque_marker=cvnfm-2\" or VM VNFM token"
                                                                   + " e.g. \"nextpage_opaque_marker=vmvnfm-2020-04-06T13"
                                                                   + ":54"
                                                                   + ":37.786Z\".",

                                                           opaqueMarker),
                                             HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                             HttpStatus.BAD_REQUEST.value());
        } else {
            RequestService requestService = paginatedRequestServiceCache.get(NEXTPAGE_OPAQUE_MARKER_CVNFM_PATTERN);
            LOGGER.info(LOG_ROUTER, requestService.getClass());
            return requestService;
        }
    }
}
