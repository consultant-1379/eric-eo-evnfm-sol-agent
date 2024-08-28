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
package com.ericsson.eoevnfmnbi.filters;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.eoevnfmnbi.utils.Constants.PAGINATION_INFO_HEADER;

import static utils.CommonUtils.createMockEvnfmResponse;
import static utils.CommonUtils.createMockResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.kubernetes.commons.KubernetesCommonsAutoConfiguration;
import org.springframework.cloud.kubernetes.fabric8.Fabric8AutoConfiguration;
import org.springframework.cloud.kubernetes.fabric8.config.reload.Fabric8ConfigReloadAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.ericsson.eoevnfmnbi.ApplicationServer;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ApplicationServer.class)
@TestPropertySource(
    properties = {
        "hosts.primary=http://localhost:10108",
        "hosts.secondary=http://localhost:10109",
        "spring.cloud.kubernetes.enabled = false"
    })
@ActiveProfiles("dev")
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@EnableAutoConfiguration(exclude = { Fabric8AutoConfiguration.class, Fabric8ConfigReloadAutoConfiguration.class,
    KubernetesCommonsAutoConfiguration.class })
public class MergeResponseGatewayFilterFactoryCodeployTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeResponseGatewayFilterFactoryCodeployTest.class);
    private MockWebServer mockWebServerEvnfm;
    private MockWebServer mockWebServerVnflcm;
    private static String jsonBodyFromEvnfm;
    private static String jsonPaginatedBodyFromEvnfm;
    private static String jsonPaginatedBodyFromEvnfmCustomPageSize;
    private static String jsonBodyFromVnflcm;
    private static String jsonPaginatedBodyFromVnflcm;
    private static String jsonPaginatedBodyFromVnflcmCustomPageSize;
    private static String jsonErrorPageSize;
    private static String jsonErrorEmptyPageSize;
    private static String jsonErrorInvalidPageSize;
    private static String jsonErrorNegativePageSize;
    private static String jsonErrorNextpageOpaqueMarker;
    private static String jsonErrorEmptyNextpageOpaqueMarker;
    private static String jsonErrorFromVmVnfm;
    private static String jsonErrorFromCvnfm;
    private static final String BOTH_VNF_PATH = "/vnflcm/v1/vnf_instances";
    private static final String BOTH_VNF_PATH_PAGE_SIZE = "/vnflcm/v1/vnf_instances?size=10";

    private static final String CNF_PATH_NEXTPAGE_MARKER = "/vnflcm/v1/vnf_instances?nextpage_opaque_marker=cvnfm-2";
    private static final String CNF_PATH_NEXTPAGE_MARKER_PAGE_SIZE = "/vnflcm/v1/vnf_instances?nextpage_opaque_marker=cvnfm-3"
        + "&size=10";

    private static final String VNF_PATH_PAGE_SIZE_NEXTPAGE_MARKER =
        "/vnflcm/v1/vnf_instances?size=10&nextpage_opaque_marker=vmvnfm-2020-04-06T13"
            + ":54:37.786Z";
    private static final String VNF_PATH_NEXTPAGE_MARKER = "/vnflcm/v1/vnf_instances?nextpage_opaque_marker=vmvnfm-2020-04"
        + "-06T13:54:37.786Z";
    private static final String VNF_PATH_PAGE_SIZE_FIRST_NEXTPAGE_MARKER = "/vnflcm/v1/vnf_instances?size=10"
        + "&nextpage_opaque_marker=vmvnfm";

    private static final String VNF_PATH_PAGE_EMPTY_SIZE = "/vnflcm/v1/vnf_instances?size=";

    private static final String VNF_PATH_PAGE_EMPTY_NEXT_PAGE_OPAQUE_MARKER = "/vnflcm/v1/vnf_instances?nextpage_opaque_marker=";

    private static final String VNF_PATH_PAGE_WRONG_PAGE_SIZE = "/vnflcm/v1/vnf_instances?size=101";
    private static final String VNF_PATH_PAGE_INVALID_INTEGER_PAGE_SIZE = "/vnflcm/v1/vnf_instances?size=randomstring";
    private static final String VNF_PATH_PAGE_NEGATIVE_PAGE_SIZE = "/vnflcm/v1/vnf_instances?size=-1";
    private static final String VNF_PATH_PAGE_NUMERIC_OPAQUE_MARKER = "/vnflcm/v1/vnf_instances?nextpage_opaque_marker=2";
    private static final String VNF_PATH = "/vnflcm/v1/vnf_instances?type=VNF";
    private static final String CNF_PATH = "/vnflcm/v1/vnf_instances?type=CNF";

    @Value("${hosts.primary}")
    private String evnfmUri;

    @Value("${hosts.secondary}")
    private String vnflcmUri;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    public static void setup() {
        try {
            jsonBodyFromEvnfm = Resources.toString(
                Resources.getResource("vnfInstancesResponsefromEvnfm.json"), Charsets.UTF_8);
            jsonPaginatedBodyFromEvnfm = Resources.toString(
                Resources.getResource("paginatedVnfInstanceResponsefromEvnfm.json"), Charsets.UTF_8);
            jsonPaginatedBodyFromEvnfmCustomPageSize = Resources.toString(
                Resources.getResource("paginatedVnfInstanceResponseFromEvnfmCustomPageSize.json"), Charsets.UTF_8);
            jsonBodyFromVnflcm = Resources.toString(
                Resources.getResource("vnfInstancesResponsefromVnflcm.json"), Charsets.UTF_8);
            jsonPaginatedBodyFromVnflcm = Resources.toString(
                Resources.getResource("paginatedVnfInstanceResponsefromVnflcm.json"), Charsets.UTF_8);
            jsonPaginatedBodyFromVnflcmCustomPageSize = Resources.toString(
                Resources.getResource("paginatedVnfInstanceResponseFromVnflcmCustomPageSize.json"), Charsets.UTF_8);
            jsonErrorPageSize = Resources.toString(
                Resources.getResource("errorResponsePageSize.json"), Charsets.UTF_8);
            jsonErrorNextpageOpaqueMarker = Resources.toString(
                Resources.getResource("errorResponseNextpageOpaqueMarker.json"), Charsets.UTF_8);
            jsonErrorEmptyNextpageOpaqueMarker = Resources.toString(
                Resources.getResource("errorResponseEmptyNextpageOpaqueMarker.json"), Charsets.UTF_8);
            jsonErrorEmptyPageSize = Resources.toString(
                Resources.getResource("errorResponseEmptyPageSize.json"), Charsets.UTF_8);
            jsonErrorInvalidPageSize = Resources.toString(
                Resources.getResource("errorResponseInvalidPageSize.json"), Charsets.UTF_8);
            jsonErrorNegativePageSize = Resources.toString(
                Resources.getResource("errorResponseNegativePageSize.json"), Charsets.UTF_8);
            jsonErrorFromVmVnfm = Resources.toString(
                Resources.getResource("errorResponseFromVmVnfm.json"), Charsets.UTF_8);
            jsonErrorFromCvnfm = Resources.toString(
                Resources.getResource("errorResponseFromCvnfm.json"), Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.info("Setup failed {}", e.getMessage());
        }
    }

    @BeforeEach
    public void setupEach() throws URISyntaxException, IOException {
        mockWebServerEvnfm = new MockWebServer();
        mockWebServerEvnfm.start(new URI(evnfmUri).getPort());
        mockWebServerVnflcm = new MockWebServer();
        mockWebServerVnflcm.start(new URI(vnflcmUri).getPort());
    }

    @AfterEach
    public void cleanUp() throws IOException {
        mockWebServerEvnfm.shutdown();
        mockWebServerVnflcm.shutdown();
    }

    @Test
    public void shouldReturnCnfInstancesWhenRequestingPageWithoutNextPageOpaqueMarker() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED,
                                                           jsonPaginatedBodyFromEvnfm,
                                                           "number=2,size=5,totalPages=5,totalElements=22", getCvnfmLinks()));
        webTestClient.get()
            .uri(BOTH_VNF_PATH)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getCvnfmNbiLinks());
                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(15);
            });
        RecordedRequest request = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnCnfInstancesWhenRequestingPageWithoutNextPageOpaqueMarkerWithPageSize() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED,
                                                           jsonPaginatedBodyFromEvnfmCustomPageSize,
                                                           "number=2,size=5,totalPages=5,totalElements=22", getCvnfmLinks()));
        webTestClient.get()
            .uri(BOTH_VNF_PATH_PAGE_SIZE)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getCvnfmNbiLinks());
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();
                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(10);
            });
        RecordedRequest request = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=10").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnCnfInstancesWhenRequestingPageWithNextPageOpaqueMarker() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED,
                                                           jsonPaginatedBodyFromEvnfm,
                                                           "number=2,size=5,totalPages=5,totalElements=22", getCvnfmLinks()));
        webTestClient.get()
            .uri(CNF_PATH_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getCvnfmNbiLinks());
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();
                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(15);
            });
        RecordedRequest request = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15&nextpage_opaque_marker=2").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnCnfInstancesWhenRequestingPageWithNextPageOpaqueMarkerAndPageSize() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED,
                                                           jsonPaginatedBodyFromEvnfmCustomPageSize,
                                                           "number=2,size=5,totalPages=5,totalElements=22", getCvnfmLinks()));
        webTestClient.get()
            .uri(CNF_PATH_NEXTPAGE_MARKER_PAGE_SIZE)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getCvnfmNbiLinks());
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();
                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(10);
            });
        RecordedRequest request = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=10&nextpage_opaque_marker=3").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnVnfInstancesWhenRequestingFirstPageButThereIsNoItemsInCvnfm() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, "[]", "number=1,size=5,totalPages=1,totalElements=0", getCvnfmLinks()
        ));
        mockWebServerVnflcm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonPaginatedBodyFromVnflcm, "", getVmVnfmLinks()));
        webTestClient.get()
            .uri(BOTH_VNF_PATH)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getVmVnfmNbiLinks());
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(15);
            });
        RecordedRequest cvnfmRequest = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15").isEqualTo(cvnfmRequest.getPath());
        RecordedRequest vnfmRequest = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15").isEqualTo(vnfmRequest.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnVnfInstancesWhenRequestingFirstPageButThereIsNoItemsInCvnfmAndVmVnfm() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, "[]", "number=1,size=5,totalPages=1,totalElements=0", getCvnfmLinks()
        ));
        mockWebServerVnflcm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, "[]", "", ""));
        webTestClient.get()
            .uri(BOTH_VNF_PATH)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isNull();
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(0);
            });
        RecordedRequest cvnfmRequest = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15").isEqualTo(cvnfmRequest.getPath());
        RecordedRequest vnfmRequest = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15").isEqualTo(vnfmRequest.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnErrorWhenRequestingFirstPageButThereIsNoItemsInCvnfmAndVmVnfmThrowsError() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, "[]", "number=1,size=5,totalPages=1,totalElements=0", getCvnfmLinks()
        ));
        mockWebServerVnflcm.enqueue(createMockResponse(HttpStatus.BAD_REQUEST, jsonErrorFromVmVnfm));
        webTestClient.get()
            .uri(BOTH_VNF_PATH)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorFromVmVnfm);
        RecordedRequest cvnfmRequest = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15").isEqualTo(cvnfmRequest.getPath());
        RecordedRequest vnfmRequest = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15").isEqualTo(vnfmRequest.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnErrorWhenRequestingCvnfmPageWhenCvnfmReturnsError() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockResponse(HttpStatus.BAD_REQUEST, jsonErrorFromCvnfm));

        webTestClient.get()
            .uri(CNF_PATH_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorFromCvnfm);
        RecordedRequest cvnfmRequest = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15&nextpage_opaque_marker=2").isEqualTo(cvnfmRequest.getPath());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnErrorWhenRequestingVmVnfmPageWhenVmVnfmReturnsError() throws InterruptedException {
        mockWebServerVnflcm.enqueue(createMockResponse(HttpStatus.BAD_REQUEST, jsonErrorFromVmVnfm));

        webTestClient.get()
            .uri(VNF_PATH_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorFromVmVnfm);
        RecordedRequest cvnfmRequest = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15&nextpage_opaque_marker=2020-04-06T13:54:37.786Z").isEqualTo(cvnfmRequest.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
    }

    @Test
    public void shouldReturnCnfInstancesWithVmVnfmNextLinkHeaderWhenItsLastCvnfmPageAndVmVnfmHasItems() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonPaginatedBodyFromEvnfm, "number=5,size=5,totalPages=5,"
            + "totalElements=25", getCvnfmLastPageLinks()
        ));
        mockWebServerVnflcm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonPaginatedBodyFromVnflcm, "", getVmVnfmLinks()));
        webTestClient.get()
            .uri(CNF_PATH_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getCvnfmNbiLastPageLinks());
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(15);
            });
        RecordedRequest cvnfmRequest = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=1").isEqualTo(cvnfmRequest.getPath());
        RecordedRequest vnfmRequest = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15&nextpage_opaque_marker=2").isEqualTo(vnfmRequest.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnCnfInstancesWithVmVnfmNextLinkHeaderWhenItsLastCvnfmPageAndVmVnfmHasNoItems() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonPaginatedBodyFromEvnfm, "number=5,size=5,totalPages=5,"
            + "totalElements=25", getCvnfmLastPageLinks()
        ));
        mockWebServerVnflcm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, "[]", "", getVmVnfmLinks()));
        webTestClient.get()
            .uri(CNF_PATH_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getCvnfmNbiLastPageVmVnfmNoItemsLinks());
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(15);
            });
        RecordedRequest cvnfmRequest = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=1").isEqualTo(cvnfmRequest.getPath());
        RecordedRequest vnfmRequest = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15&nextpage_opaque_marker=2").isEqualTo(vnfmRequest.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnCnfInstancesWithVmVnfmNextLinkHeaderWhenItsLastCvnfmPageAndVmVnfmThrowsError() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonPaginatedBodyFromEvnfm, "number=5,size=5,totalPages=5,"
            + "totalElements=25", getCvnfmLastPageLinks()
        ));
        mockWebServerVnflcm.enqueue(createMockResponse(HttpStatus.UNAUTHORIZED, ""));
        webTestClient.get()
            .uri(CNF_PATH_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getCvnfmNbiLastPageLinks());
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(15);
            });
        RecordedRequest cvnfmRequest = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=1").isEqualTo(cvnfmRequest.getPath());
        RecordedRequest vnfmRequest = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15&nextpage_opaque_marker=2").isEqualTo(vnfmRequest.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnVnfInstancesRequestingPageWithFirstNextPageOpaqueMarker() throws InterruptedException {
        mockWebServerVnflcm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonPaginatedBodyFromVnflcmCustomPageSize, "", getVmVnfmLinks()));
        webTestClient.get()
            .uri(VNF_PATH_PAGE_SIZE_FIRST_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getVmVnfmNbiLinks());

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(10);
            });
        RecordedRequest request = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=10").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
    }

    @Test
    public void shouldReturnVnfInstancesRequestingPageWithNextPageOpaqueMarkerAndPageSize() throws InterruptedException {
        mockWebServerVnflcm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED,
                                                            jsonPaginatedBodyFromVnflcmCustomPageSize,
                                                            "",
                                                            getVmVnfmLinksLastPage()));
        webTestClient.get()
            .uri(VNF_PATH_PAGE_SIZE_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isEqualTo(getVmVnfmNbiLinksLastPage());

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(10);
            });
        RecordedRequest request = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=10&nextpage_opaque_marker=2020-04-06T13:54:37.786Z").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
    }

    @Test
    public void shouldReturnVnfInstancesWithoutHeaderRequestingPageWithNextPageOpaqueMarkerAndPageSize() throws InterruptedException {
        mockWebServerVnflcm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonPaginatedBodyFromVnflcmCustomPageSize, "", ""));
        webTestClient.get()
            .uri(VNF_PATH_PAGE_SIZE_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.LINK)).isNull();
                assertThat(result.getResponseHeaders().getFirst(PAGINATION_INFO_HEADER)).isNull();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(10);
            });
        RecordedRequest request = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=10&nextpage_opaque_marker=2020-04-06T13:54:37.786Z").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
    }

    @Test
    public void shouldReturnVnfInstancesRequestingPageWithNextPageOpaqueMarker() throws InterruptedException {
        mockWebServerVnflcm.enqueue(createMockResponse(HttpStatus.ACCEPTED, jsonPaginatedBodyFromVnflcm));
        webTestClient.get()
            .uri(VNF_PATH_NEXTPAGE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(15);
            });
        RecordedRequest request = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?size=15&nextpage_opaque_marker=2020-04-06T13:54:37.786Z").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
    }

    @Test
    public void shouldReturnVnfInstancesRequestingVnfType() throws InterruptedException {
        mockWebServerVnflcm.enqueue(createMockResponse(HttpStatus.ACCEPTED, jsonBodyFromVnflcm));
        webTestClient.get()
            .uri(VNF_PATH)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(291);
            });
        RecordedRequest request = mockWebServerVnflcm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerVnflcm.getRequestCount());
    }

    @Test
    public void shouldReturnCnfInstancesRequestingCnfType() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonBodyFromEvnfm, "number=2,size=5,totalPages=5,totalElements=22",
                                                           getCvnfmLinks()));
        webTestClient.get()
            .uri(CNF_PATH)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(16);
            });
        RecordedRequest request = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnCnfInstancesRequestingNextOpaquePageMarkerAsNumber() throws InterruptedException {
        mockWebServerEvnfm.enqueue(createMockEvnfmResponse(HttpStatus.ACCEPTED, jsonBodyFromEvnfm, "number=2,size=5,totalPages=5,totalElements=22",
                                                           getCvnfmLinks()));
        webTestClient.get()
            .uri(VNF_PATH_PAGE_NUMERIC_OPAQUE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();

                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(16);
            });
        RecordedRequest request = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?nextpage_opaque_marker=2").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
    }

    @Test
    public void shouldReturnErrorWhenPageSizeExceedsMax() {
        webTestClient.get()
            .uri(VNF_PATH_PAGE_WRONG_PAGE_SIZE)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorPageSize);
    }

    @Test
    public void shouldReturnErrorWhenPageSizeInvalidInteger() {
        webTestClient.get()
            .uri(VNF_PATH_PAGE_INVALID_INTEGER_PAGE_SIZE)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorInvalidPageSize);
    }

    @Test
    public void shouldReturnErrorWhenPageSizeIsNegative() {
        webTestClient.get()
            .uri(VNF_PATH_PAGE_NEGATIVE_PAGE_SIZE)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorNegativePageSize);
    }

    @Test
    @Disabled("Should be turned on once legacy behaviour is removed")
    public void shouldReturnErrorWhenWrongNextpageOpaqueMarker() {
        webTestClient.get()
            .uri(VNF_PATH_PAGE_NUMERIC_OPAQUE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorNextpageOpaqueMarker);
    }

    @Test
    public void shouldReturnCnfInstancesWhenRequestingPageWithEmptyPageSize() throws InterruptedException {
        webTestClient.get()
            .uri(VNF_PATH_PAGE_EMPTY_SIZE)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorEmptyPageSize);
    }

    @Test
    public void shouldReturnErrorWhenRequestingPageWithEmptyOpaqueMarker() {
        webTestClient.get()
            .uri(VNF_PATH_PAGE_EMPTY_NEXT_PAGE_OPAQUE_MARKER)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json(jsonErrorEmptyNextpageOpaqueMarker);
    }

    private String getCvnfmLinks() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=1>;rel=\"first\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=1>;rel=\"previous\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=2>;rel=\"self\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=3>;rel=\"next\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=5>;rel=\"last\"";
    }

    private String getCvnfmNbiLinks() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5>;rel=\"first\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=cvnfm-2>;"
            + "rel=\"self\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=cvnfm-3>;rel=\"next\"";
    }

    private String getCvnfmLastPageLinks() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=1>;rel=\"first\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=4>;rel=\"previous\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=5>;rel=\"self\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=5>;rel=\"last\"";
    }

    private String getCvnfmNbiLastPageLinks() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5>;rel=\"first\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=cvnfm-5>;"
            + "rel=\"self\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=vmvnfm>;rel=\"next\"";
    }

    private String getCvnfmNbiLastPageVmVnfmNoItemsLinks() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5>;rel=\"first\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=cvnfm-5>;"
            + "rel=\"self\"";
    }

    private String getVmVnfmLinks() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=2020-04-06T13:54:37"
            + ".786Z>; rel=\"self\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=2020-04-06T13:56:37"
            + ".786Z>; rel=\"next\"";
    }

    private String getVmVnfmLinksLastPage() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=2020-04-06T13:54:37"
            + ".786Z>; rel=\"self\"";
    }

    private String getVmVnfmNbiLinks() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5>;rel=\"first\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=vmvnfm-2020-04-06T13"
            + ":54:37.786Z>;rel=\"self\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=vmvnfm-2020-04-06T13"
            + ":56:37.786Z>;rel=\"next\"";
    }

    private String getVmVnfmNbiLinksLastPage() {
        return "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5>;rel=\"first\"," +
            "<http://vnfm.ekhavit.haber002-iccr.ews.gic.ericsson.se/vnflcm/v1/vnf_instances?size=5&nextpage_opaque_marker=vmvnfm-2020-04-06T13"
            + ":54:37.786Z>;rel=\"self\"";
    }
}
