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

import static utils.CommonUtils.createMockResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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
        "hosts.secondary=",
        "spring.cloud.kubernetes.enabled = false"
    })
@ActiveProfiles("dev")
@DirtiesContext
@ExtendWith(MockitoExtension.class)
public class MergeResponseGatewayFilterFactoryStandaloneTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeResponseGatewayFilterFactoryStandaloneTest.class);

    private MockWebServer mockWebServerEvnfm;

    private static String jsonBodyFromEvnfm;

    @Value("${hosts.primary}")
    private String evnfmUri;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    public static void setup() {
        try {
            jsonBodyFromEvnfm = Resources.toString(Resources.getResource("vnfInstancesResponsefromEvnfm.json"), Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.info("Setup failed {}", e.getMessage());
        }
    }

    @BeforeEach
    public void setupEach() throws URISyntaxException, IOException {
        mockWebServerEvnfm = new MockWebServer();
        mockWebServerEvnfm.start(new URI(evnfmUri).getPort());
    }

    @AfterEach
    public void cleanUp() throws IOException {
        mockWebServerEvnfm.shutdown();
    }

    @Test
    public void shouldReturnCnfInstances() throws InterruptedException {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJncWFMd1h0TG5UbldULXpJTW1KSm43OE15MWRESm8xWmwwSFplcThiUHdZIn0";
        mockWebServerEvnfm.enqueue(createMockResponse(HttpStatus.ACCEPTED, jsonBodyFromEvnfm));
        webTestClient.post()
            .uri("/vnflcm/v1/vnf_instances?filter=(eq,addedToOss,false)")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, token)
            .exchange()
            .expectBody(Object[].class)
            .consumeWith(result -> {
                assertThat(result).isNotNull();
                Object[] responseBody = result.getResponseBody();
                assertThat(responseBody).isNotNull();
                assertThat(responseBody.length).isEqualTo(16);
            });
        RecordedRequest request = mockWebServerEvnfm.takeRequest();
        assertThat("/vnflcm/v1/vnf_instances?filter=(eq,addedToOss,false)").isEqualTo(request.getPath());
        assertThat(1).isEqualTo(mockWebServerEvnfm.getRequestCount());
        assertThat(token).isEqualTo(request.getHeader(HttpHeaders.AUTHORIZATION));
    }
}
