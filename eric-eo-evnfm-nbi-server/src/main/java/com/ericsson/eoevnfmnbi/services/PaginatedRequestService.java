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

import static com.ericsson.eoevnfmnbi.utils.Constants.DEFAULT_PAGE_SIZE;
import static com.ericsson.eoevnfmnbi.utils.Constants.MAX_PAGE_SIZE;
import static com.ericsson.eoevnfmnbi.utils.Constants.MIN_PAGE_SIZE;
import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_PARAM;
import static com.ericsson.eoevnfmnbi.utils.Constants.PAGE_SIZE_PARAM;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.eoevnfmnbi.exceptions.DefaultCustomException;

public interface PaginatedRequestService extends RequestService {

    default void setPageSize(MultiValueMap<String, String> queryParams, UriComponentsBuilder builder) {
        int pageSize;
        if (queryParams.containsKey(PAGE_SIZE_PARAM)) {
            String pageSizeString = queryParams.getFirst(PAGE_SIZE_PARAM);
            if (StringUtils.isNotEmpty(pageSizeString) && tryParsePageSize(pageSizeString)) {
                pageSize = Integer.parseInt(pageSizeString);

                if (pageSize > MAX_PAGE_SIZE) {
                    throw new DefaultCustomException("Wrong query parameter",
                                                     String.format("Total size of the results will be shown cannot be more than 100. Requested"
                                                                           + " page size %s", pageSize),
                                                     HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                     HttpStatus.BAD_REQUEST.value());
                } else if (pageSize < MIN_PAGE_SIZE) {
                    throw new DefaultCustomException("Wrong query parameter",
                                                     String.format("Invalid page number:: %s, page number must be greater than 0", pageSize),
                                                     HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                     HttpStatus.BAD_REQUEST.value());
                }
            } else {
                throw new DefaultCustomException("Wrong query parameter",
                                                 String.format(
                                                         "Invalid page value for nextpage_opaque_marker:: %s",
                                                         pageSizeString),
                                                 HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                 HttpStatus.BAD_REQUEST.value());
            }
        } else {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        builder.replaceQueryParam(PAGE_SIZE_PARAM, pageSize);
    }

    default void setNextPageOpaqueMarker(MultiValueMap<String, String> queryParams, UriComponentsBuilder builder) {
        String opaqueMarker = queryParams.getFirst(NEXTPAGE_OPAQUE_MARKER_PARAM);
        if (StringUtils.isNotEmpty(opaqueMarker)) {
            Matcher matcher = getNextPageOpaqueMarkerPattern().matcher(opaqueMarker);
            if (matcher.find() && matcher.groupCount() >= 1) {
                String nextPageOpaqueMarker = matcher.group(1);
                if (StringUtils.isNotEmpty(nextPageOpaqueMarker)) {
                    builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM, nextPageOpaqueMarker);
                } else {
                    builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM);
                }
            } else {
                builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM);
            }
        } else {
            builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM);
        }
    }

    default boolean tryParsePageSize(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    Pattern getNextPageOpaqueMarkerPattern();
}
