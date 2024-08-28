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

import static com.ericsson.eoevnfmnbi.utils.Constants.NUMBER_PARAM;
import static com.ericsson.eoevnfmnbi.utils.Constants.PAGINATION_INFO_HEADER;
import static com.ericsson.eoevnfmnbi.utils.Constants.TOTAL_ELEMENTS_PARAM;
import static com.ericsson.eoevnfmnbi.utils.Constants.TOTAL_PAGES_PARAM;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

public final class PaginationInfoUtils {

    private PaginationInfoUtils() {

    }

    public static int getTotalElementsFromResponseHeader(HttpHeaders headers) {
        int totalElements = 0;
        String paginationHeader = headers.getFirst(PAGINATION_INFO_HEADER);
        if (StringUtils.isNotEmpty(paginationHeader)) {
            String[] pag = paginationHeader.split(",");
            for (String link : pag) {
                String[] value = link.split("=");
                if (Objects.equals(value[0], TOTAL_ELEMENTS_PARAM)) {
                    return Integer.parseInt(value[1]);
                }
            }
        }
        return totalElements;
    }

    public static boolean isLastPage(HttpHeaders headers) {
        int totalPages = 0;
        int number = 0;
        String paginationHeader = headers.getFirst(PAGINATION_INFO_HEADER);
        if (StringUtils.isNotEmpty(paginationHeader)) {
            String[] pag = paginationHeader.split(",");
            for (String link : pag) {
                String[] value = link.split("=");
                if (Objects.equals(value[0], TOTAL_PAGES_PARAM)) {
                    totalPages = Integer.parseInt(value[1]);
                }
                if (Objects.equals(value[0], NUMBER_PARAM)) {
                    number = Integer.parseInt(value[1]);
                }
            }
        }
        return totalPages != 0 && totalPages == number;
    }
}
