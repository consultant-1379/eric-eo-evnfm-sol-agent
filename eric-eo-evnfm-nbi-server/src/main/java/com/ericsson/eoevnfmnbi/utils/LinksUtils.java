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

import static com.ericsson.eoevnfmnbi.utils.Constants.FIRST_LINK_REL;
import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_CVNFM_PREFIX;
import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_FIRST_PAGE_VMVNFM_PREFIX;
import static com.ericsson.eoevnfmnbi.utils.Constants.NEXTPAGE_OPAQUE_MARKER_PARAM;
import static com.ericsson.eoevnfmnbi.utils.Constants.NEXT_LINK_REL;
import static com.ericsson.eoevnfmnbi.utils.Constants.REL_LINK_PATTERN;
import static com.ericsson.eoevnfmnbi.utils.Constants.REL_LINK_PLACEHOLDER;
import static com.ericsson.eoevnfmnbi.utils.Constants.SELF_LINK_REL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public final class LinksUtils {

    private LinksUtils() {}

    public static String buildLinks(Map<String, String> links, String paginationPrefix) {
        String selfLinkUri = links.get(SELF_LINK_REL);
        String nextLinkUri = links.get(NEXT_LINK_REL);
        List<String> linksList = new ArrayList<>();
        if (StringUtils.isNotEmpty(selfLinkUri)) {
            String firsLink = String.format(REL_LINK_PLACEHOLDER, buildFirstUri(selfLinkUri), FIRST_LINK_REL);
            String selfLink = String.format(REL_LINK_PLACEHOLDER, buildSelfUri(selfLinkUri, paginationPrefix), SELF_LINK_REL);
            linksList.add(firsLink);
            linksList.add(selfLink);
        }
        if (StringUtils.isNotEmpty(nextLinkUri)) {
            String nextLink = String.format(REL_LINK_PLACEHOLDER, buildNextUri(nextLinkUri, paginationPrefix), NEXT_LINK_REL);
            linksList.add(nextLink);
        } else if (NEXTPAGE_OPAQUE_MARKER_FIRST_PAGE_VMVNFM_PREFIX.equals(paginationPrefix)) {
            String nextLink = String.format(REL_LINK_PLACEHOLDER, buildNextUri(selfLinkUri, paginationPrefix), NEXT_LINK_REL);
            linksList.add(nextLink);
        }
        return String.join(",", linksList);
    }

    public static Map<String, String> parseLinks(String linkHeader) {
        Map<String, String> links = new HashMap<>();
        if (StringUtils.isNotEmpty(linkHeader)) {
            Matcher matcher = REL_LINK_PATTERN.matcher(linkHeader);
            while (matcher.find()) {
                String uri = matcher.group(1);
                String rel = matcher.group(2);
                links.put(rel, uri);
            }
        }
        return links;
    }

    private static String buildFirstUri(String uri) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);
        builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM);
        return builder.toUriString();
    }

    private static String buildSelfUri(String uri, String paginationPrefix) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);
        MultiValueMap<String, String> queryParams = builder.build().getQueryParams();
        if (NEXTPAGE_OPAQUE_MARKER_FIRST_PAGE_VMVNFM_PREFIX.equals(paginationPrefix)) {
            builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM,
                                      NEXTPAGE_OPAQUE_MARKER_CVNFM_PREFIX + queryParams.getFirst(NEXTPAGE_OPAQUE_MARKER_PARAM));
        } else {
            builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM, paginationPrefix + queryParams.getFirst(NEXTPAGE_OPAQUE_MARKER_PARAM));
        }
        return builder.toUriString();
    }

    private static String buildNextUri(String uri, String paginationPrefix) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);
        MultiValueMap<String, String> queryParams = builder.build().getQueryParams();
        if (NEXTPAGE_OPAQUE_MARKER_FIRST_PAGE_VMVNFM_PREFIX.equals(paginationPrefix)) {
            builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM, paginationPrefix);
        } else {
            builder.replaceQueryParam(NEXTPAGE_OPAQUE_MARKER_PARAM, paginationPrefix + queryParams.getFirst(NEXTPAGE_OPAQUE_MARKER_PARAM));
        }
        return builder.toUriString();
    }
}
