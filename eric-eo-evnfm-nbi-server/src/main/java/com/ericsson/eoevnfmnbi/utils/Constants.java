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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;

public final class Constants {

    public static final String VNF_INSTANCES = "vnf_instances";
    public static final String VNF_DESCRIPTOR = "vnf_descriptor";
    public static final String VNF_OCCURRENCES = "vnf_occurrences";
    public static final String VNF = "VNF";
    public static final String CNF = "CNF";
    public static final String VNF_INSTANCE_ID = "vnfInstanceId";
    public static final String VNF_OCCURRENCE_ID = "vnflcmOpOccId";

    public static final String GATEWAY_TIMEOUT_EXCEPTION_MESSAGE = "504 GATEWAY_TIMEOUT";
    public static final String TITLE_RESPONSE_PARAM_KEY = "title";
    public static final String DETAIL_RESPONSE_PARAM_KEY = "detail";
    public static final String TYPE_RESPONSE_PARAM_KEY = "type";
    public static final String STATUS_RESPONSE_PARAM_KEY = "status";
    public static final List<String> DYNAMIC_ERROR_RESPONSE_PARAMS = List.of(DETAIL_RESPONSE_PARAM_KEY);
    public static final String NEXTPAGE_OPAQUE_MARKER_PARAM = "nextpage_opaque_marker";
    public static final String TYPE_PARAM = "type";
    public static final String PAGE_SIZE_PARAM = "size";
    public static final String PAGINATION_INFO_HEADER = "paginationinfo";
    public static final String TOTAL_ELEMENTS_PARAM = "totalElements";
    public static final String TOTAL_PAGES_PARAM = "totalPages";
    public static final String NUMBER_PARAM = "number";
    public static final Pattern NEXTPAGE_OPAQUE_MARKER_VMVNFM_PATTERN =
            Pattern.compile("^vmvnfm(?:-(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?(Z|[+-]\\d{2}:\\d{2})))?$");
    public static final Pattern NEXTPAGE_OPAQUE_MARKER_CVNFM_PATTERN = Pattern.compile("cvnfm-(\\d+)?");
    public static final Pattern NEXTPAGE_OPAQUE_MARKER_LEGACY_CVNFM = Pattern.compile("\\d+");
    public static final String NEXTPAGE_OPAQUE_MARKER_CVNFM_PREFIX = "cvnfm-";
    public static final String NEXTPAGE_OPAQUE_MARKER_VMVNFM_PREFIX = "vmvnfm-";
    public static final String NEXTPAGE_OPAQUE_MARKER_FIRST_PAGE_VMVNFM_PREFIX = "vmvnfm";
    public static final int DEFAULT_PAGE_SIZE = 15;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 1;
    public static final String SELF_LINK_REL = "self";
    public static final String NEXT_LINK_REL = "next";
    public static final String FIRST_LINK_REL = "first";
    public static final String REL_LINK_PLACEHOLDER = "<%s>;rel=\"%s\"";
    public static final Pattern REL_LINK_PATTERN = Pattern.compile("<(.*?)>;\\s*rel=\"(.*?)\"");

    public static final ParameterizedTypeReference<List<Object>> PARAMETERIZED_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    public static final Map<String, Object> TIMEOUT_ERROR_RESPONSE_PARAMS = Map.of(TITLE_RESPONSE_PARAM_KEY, "Request timeout",
                                                               DETAIL_RESPONSE_PARAM_KEY, "%s service gateway timeout",
                                                               TYPE_RESPONSE_PARAM_KEY, HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase(),
                                                               STATUS_RESPONSE_PARAM_KEY, HttpStatus.GATEWAY_TIMEOUT.value());
    public static final Map<String, Object> SERVICE_UNAVAILABLE_ERROR_RESPONSE_PARAMS = Map
            .of(TITLE_RESPONSE_PARAM_KEY, "Connection failure",
                DETAIL_RESPONSE_PARAM_KEY, "%s service temporarily unavailable",
                TYPE_RESPONSE_PARAM_KEY, HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                STATUS_RESPONSE_PARAM_KEY, HttpStatus.SERVICE_UNAVAILABLE.value());

    private Constants() {}
}
