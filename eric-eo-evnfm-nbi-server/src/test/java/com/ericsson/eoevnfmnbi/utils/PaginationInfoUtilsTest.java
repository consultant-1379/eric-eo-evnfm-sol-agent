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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class PaginationInfoUtilsTest {

    @Test
    void getTotalElementsFromEmptyResponseHeader() {
        assertEquals(0, PaginationInfoUtils.getTotalElementsFromResponseHeader(new HttpHeaders()));
    }

    @Test
    void isLastPageFromEmptyResponseHeader() {
        assertFalse(PaginationInfoUtils.isLastPage(new HttpHeaders()));
    }
}