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
package utils;

import static com.ericsson.eoevnfmnbi.utils.Constants.PAGINATION_INFO_HEADER;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import com.ericsson.eoevnfmnbi.config.NfvoConfig;
import com.ericsson.eoevnfmnbi.config.OccurrenceConfig;
import com.ericsson.eoevnfmnbi.config.OnboardingConfig;
import com.ericsson.eoevnfmnbi.config.VnfConfig;
import com.ericsson.eoevnfmnbi.config.VnfmConfig;
import com.ericsson.eoevnfmnbi.config.retrytemplate.RetryTemplateConfig;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import okhttp3.Headers;
import okhttp3.mockwebserver.MockResponse;

public final class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    public static final String EVNFM_URI = "http://localhost:10102";
    public static final String VNFLCM_URI = "http://localhost:10104";
    public static final String VNFM_VNF_INSTANCES_ID_PATH = "/vnflcm/v1/vnf_instances/{vnfInstanceId}";
    public static final String OCCURRENCE_PATH = "/vnflcm/v1/vnf_lcm_op_occs/{vnflcmOpOccId}";
    public static final String UNSUPPORTED_PATH = "/vnflcm/v1/vnf_instances/{vnfInstanceId}/update";

    private static final String ONBOARDING_PATH = "/api/vnfpkgm/v1/vnf_packages";
    private static final String ONBOARDING_QUERY_VALUE_EVNFM = "(eq,vnfdId,%s)";
    private static final String ONBOARDING_QUERY_VALUE_NFVO = "vnfdId.eq=%s&softwareImages.containerFormat.eq=DOCKER";

    private CommonUtils() {
        //not called
    }

    public static MockServerHttpRequest getMockServerHttpRequest(String path, Object... uriVariables) {
        return MockServerHttpRequest
                .get(path, uriVariables)
                .build();
    }

    public static MockServerHttpRequest postMockServerHttpRequest(String path, String body, Object... uriVariables) {
        MockServerHttpRequest.BodyBuilder bodyBuilder = MockServerHttpRequest.post(path, uriVariables);
        return bodyBuilder.body(body);
    }

    public static MockResponse createMockEvnfmResponse(final HttpStatus httpStatus, String body, String paginationInfo,
                                                       String links) {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (StringUtils.isNotEmpty(paginationInfo)) {
            headersMap.put(PAGINATION_INFO_HEADER, paginationInfo);
        }
        if (StringUtils.isNotEmpty(links)) {
            headersMap.put(HttpHeaders.LINK, links);
        }
        Headers headers = Headers.of(headersMap);

        return new MockResponse()
                .setResponseCode(httpStatus.value())
                .setHeaders(headers)
                .setBody(body);
    }

    public static MockResponse createMockResponse(final HttpStatus httpStatus, String body) {
        return new MockResponse()
                .setResponseCode(httpStatus.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }

    public static Route createRoute(String id, int order, String uri) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(id);
        routeDefinition.setOrder(order);

        try {
            routeDefinition.setUri(new URI(uri));
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to create route");
        }

        Route.AsyncBuilder routeBuilder = Route.async(routeDefinition)
                .predicate(exchange -> true);

        return routeBuilder.build();
    }

    public static String parseJsonFile(String filename) {
        String jsonResponseBody = "";
        try {
            jsonResponseBody = Resources.toString(
                    Resources.getResource(filename), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonResponseBody;
    }

    public static OnboardingConfig createOnboardingConfig(String host, boolean isNfvoEnabled) {
        OnboardingConfig onboardingConfig = new OnboardingConfig();
        onboardingConfig.setHost(host);
        onboardingConfig.setPath(ONBOARDING_PATH);
        onboardingConfig.setQueryValue(isNfvoEnabled ? ONBOARDING_QUERY_VALUE_NFVO : ONBOARDING_QUERY_VALUE_EVNFM);
        return onboardingConfig;
    }

    public static VnfmConfig createVnfmConfig(String host) {
        VnfmConfig vnfmConfig = new VnfmConfig();
        vnfmConfig.setHost(host);

        VnfConfig vnfConfig = new VnfConfig();
        vnfConfig.setPath(VNFM_VNF_INSTANCES_ID_PATH);
        vnfmConfig.setVnf(vnfConfig);

        OccurrenceConfig occurrenceConfig = new OccurrenceConfig();
        occurrenceConfig.setPath(OCCURRENCE_PATH);
        vnfmConfig.setOccurrence(occurrenceConfig);

        return vnfmConfig;
    }

    public static NfvoConfig createNfvoConfig(boolean isEnabled) {
        NfvoConfig nfvoConfig = new NfvoConfig();
        nfvoConfig.setEnabled(isEnabled);
        nfvoConfig.setPassword("ecmAdmin");
        nfvoConfig.setUsername("CloudAdmin123");
        nfvoConfig.setTenantId("ECM");
        return nfvoConfig;
    }

    public static RetryTemplateConfig createRetryTemplateConfig(int maxAttempts, long backoff) {
        RetryTemplateConfig retryTemplateConfig = new RetryTemplateConfig();
        retryTemplateConfig.setMaxAttempts(maxAttempts);
        retryTemplateConfig.setBackoff(backoff);
        return retryTemplateConfig;
    }

    public static String getResourceContent(String fileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(Paths.get(Resources.getResource(fileName).toURI())));
    }
}
