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
package com.ericsson.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.ericsson.eoevnfmnbi.ApplicationServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ApplicationServer.class)
@ActiveProfiles("dev")
@AutoConfigureStubRunner(ids = { "com.ericsson.orchestration.mgmt.packaging:eric-am-onboarding-server:::10101",
    "com.ericsson.orchestration.mgmt:eric-vnfm-orchestrator-server:::10102",
    "com.ericsson.orchestration.mgmt:eric-eo-vm-vnfm:::10104" })
@TestPropertySource(properties = { "spring.hazelcast.config=classpath:hazelcast-test.yaml" })
public class NbiRouterFuncTest {

    public static final String VNF_INSTANCES_URI = "vnflcm/v1/vnf_instances";
    public static final String VNF_LCM_OPERATIONS_URI = "vnflcm/v1/vnf_lcm_op_occs";
    public static final String C_VNF_INSTANCE_NAME = "my-instance-name-1";
    public static final String VM_VNF_INSTANCE_NAME = "checkpkg1";
    public static final String C_VNF_INSTANCE_ID = "54321";
    public static final String C_VNF_INSTANCE_OCC_ID = "b08fcbc8-474f-4673-91ee-761fd83991e3";
    public static final String VM_VNF_INSTANCE_OCC_ID = "dd018d7e-524c-11ea-ae59-b61d40103d36";
    public static final String C_VNF_VNFDID = "d3def1ce-4cf4-477c-aab3-21cb04e6a378";
    public static final String VM_VNF_VNFDID = "def1ce-4cf4-477c-aab3-2b04e6a381";
    public static final String JSON_KEY_VNFD_ID = "/vnfdId";
    public static final String JSON_KEY_VNF_INSTANCE_NAME = "/vnfInstanceName";
    public static final String SUT_URL = "http://localhost:";

    @LocalServerPort
    private int port;

    @Test
    public void createVnfIdentifier() {
        String url = SUT_URL + port + "/" + VNF_INSTANCES_URI;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.set("Idempotency-key", "dummyKey");
        //C-LCM
        String vnfInstanceName = "instancename-clcm";
        final String vnfInstanceDesc = "Test Description";
        CreateIdentifier createIdentifier = new CreateIdentifier(C_VNF_VNFDID, vnfInstanceDesc, vnfInstanceName);
        HttpEntity<CreateIdentifier> requestEntity = new HttpEntity<>(createIdentifier, httpHeaders);
        ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(url, HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage("Create VNF Identifier response code for C-LCM was expected to be 201 but was %s",
                             responseEntity.getStatusCode())
            .isEqualTo(HttpStatusCode.valueOf(201));
        checkResponseAtJsonPath(responseEntity, JSON_KEY_VNFD_ID, C_VNF_VNFDID,
                                String.format("Failed to find C-LCM vnfdId in response to REST call to createIdentifier\nVnfdId: %s\nResponse: %s",
                                              C_VNF_VNFDID,
                                              responseEntity.getBody()));
        checkResponseAtJsonPath(responseEntity, JSON_KEY_VNF_INSTANCE_NAME, vnfInstanceName,
                                String.format(
                                    "Failed to find C-LCM instance name in response to REST call to createIdentifier\nVnf instance name: "
                                        + "%s\nResponse: %s",
                                    vnfInstanceName,
                                    responseEntity.getBody()));

        //VM-LCM
        vnfInstanceName = "checkpkg2";  //has to match VM-LCM stub response
        createIdentifier = new CreateIdentifier(VM_VNF_VNFDID, vnfInstanceDesc, vnfInstanceName);
        requestEntity = new HttpEntity<>(createIdentifier, httpHeaders);
        responseEntity = returnResponseEntityWithLogs(url, HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage("Create VNF Identifier response code for VM-LCM was expected to be 201 but was %s",
                             responseEntity.getStatusCode())
            .isEqualTo(HttpStatusCode.valueOf(201));
        checkResponseAtJsonPath(responseEntity, JSON_KEY_VNFD_ID, VM_VNF_VNFDID,
                                String.format("Failed to find VM-LCM vnfdId in response to REST call to createIdentifier\nVnfdId: %s\nResponse: %s",
                                              VM_VNF_VNFDID,
                                              responseEntity.getBody()));
        checkResponseAtJsonPath(responseEntity, JSON_KEY_VNF_INSTANCE_NAME, vnfInstanceName,
                                String.format(
                                    "Failed to find VM-LCM instance name in response to REST call to createIdentifier\nVnf instance name: "
                                        + "%s\nResponse: %s",
                                    vnfInstanceName,
                                    responseEntity.getBody()));
    }

    @Test
    public void queryVnfInstances() {
        String url = SUT_URL + port + "/" + VNF_INSTANCES_URI;
        ResponseEntity<String> responseEntity = doGetRestCall(url);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage("VNF instance details could not be retrieved from: %s\nResponse:: %s", url, responseEntity.getBody())
            .isEqualTo(HttpStatusCode.valueOf(200));
        checkResponseForValues(responseEntity, "vnfInstanceName",
                               List.of(C_VNF_INSTANCE_NAME),
                               String.format("VNF instance details, %s  not present in response %s",
                                             C_VNF_INSTANCE_NAME,
                                             responseEntity.getBody()));
    }

    @Test
    public void queryVnfInstanceById() {
        String url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + C_VNF_INSTANCE_ID;
        //C-LCM
        ResponseEntity<String> responseEntity = doGetRestCall(url);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage("Failed to get vnf instance details from C-LVM, url: %s\nDetail: %s", url, responseEntity.getBody())
            .isEqualTo(HttpStatusCode.valueOf(200));
        checkResponseAtJsonPath(responseEntity, JSON_KEY_VNF_INSTANCE_NAME, C_VNF_INSTANCE_NAME, "C-LCM VNF instance name not present in response");

        // VM - LCM
        String vnfInstanceId = "54321VMLCM";
        url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + vnfInstanceId;
        responseEntity = doGetRestCall(url);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage("Failed to get vnf instance details from VM-LVM, url: %s\nDetail: %s", url, responseEntity.getBody())
            .isEqualTo(HttpStatusCode.valueOf(200));
        checkResponseAtJsonPath(responseEntity, JSON_KEY_VNF_INSTANCE_NAME, VM_VNF_INSTANCE_NAME, "VM-LCM VNF instance name not present in response");
    }

    @Test
    public void instantiateVnf() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.set("Idempotency-key", "dummyKey");
        String body = "{}";
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);

        //C-LCM
        String url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + C_VNF_INSTANCE_ID + "/instantiate";
        ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(url, HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage(String.format("Instantiate vnf response code was expected to be 202 but was %S", responseEntity.getStatusCode()))
            .isEqualTo(HttpStatusCode.valueOf(202));
        //VM-LCM
        url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + C_VNF_INSTANCE_ID + "VMLCM" + "/instantiate";
        responseEntity = returnResponseEntityWithLogs(url, HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode())
            .withFailMessage(String.format("Instantiate vnf response code was expected to be 202 but was %S", responseEntity.getStatusCode()))
            .isEqualTo(HttpStatusCode.valueOf(202));
    }

    @Test
    public void upgradeVnf() {
        String url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + C_VNF_INSTANCE_ID + "/change_package_info";
        //C-LCM Only - VM-LCM uses '/change_flavour'
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.set("Idempotency-key", "dummyKey");
        String body = "{\"vnfdId\": \"d3def1ce-4cf4-477c-aab3-21cb04e6a378\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(url, HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage(String.format("Upgrade vnf response code was expected to be 202 but was %S", responseEntity.getStatusCode()))
            .isEqualTo(HttpStatusCode.valueOf(202));
    }

    @Test
    public void terminateVnf() {
        String url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + C_VNF_INSTANCE_ID + "/terminate";
        //C-LCM
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.set("Idempotency-key", "dummyKey");
        String body = "{\"terminationType\": \"FORCEFUL\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(url, HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage(String.format("Terminate C-LCM vnf response code was expected to be 202 but was %S", responseEntity.getStatusCode()))
            .isEqualTo(HttpStatusCode.valueOf(202));
        //VM-LCM
        url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + C_VNF_INSTANCE_ID + "VMLCM" + "/terminate";
        responseEntity = returnResponseEntityWithLogs(url, HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode())
            .withFailMessage(String.format("Terminate VM-LCM vnf response code was expected to be 202 but was %S",
                                           responseEntity.getStatusCode()))
            .isEqualTo(HttpStatusCode.valueOf(202));
    }

    @Test
    public void queryAllLifeCycleOperations() {
        String url = SUT_URL + port + "/" + VNF_LCM_OPERATIONS_URI;
        ResponseEntity<String> responseEntity = doGetRestCall(url);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage("Failed to get all life cycle operations, response: %s", responseEntity.getBody())
            .isEqualTo(HttpStatusCode.valueOf(200));
        checkResponseForValues(responseEntity, "id", List.of(C_VNF_INSTANCE_OCC_ID),
                               String.format("All LCM operation occurrence details could not be retrieved from: %s\nResponse: %s",
                                             url,
                                             responseEntity.getBody()));
    }

    @Test
    public void queryLifeCycleOperationById() {
        //C_LCM
        String jsonPathForId = "/_links/self/href";
        String url = SUT_URL + port + "/" + VNF_LCM_OPERATIONS_URI + "/" + C_VNF_INSTANCE_OCC_ID;
        ResponseEntity<String> responseEntity = doGetRestCall(url);
        String failMessage = String.format("Failed to get expected occurrence details from C_LCM url: %s\nDetail: %s", url, responseEntity.getBody());
        assertThat(responseEntity.getStatusCode())
            .withFailMessage(failMessage)
            .isEqualTo(HttpStatusCode.valueOf(200));
        checkResponseAtJsonPath(responseEntity, jsonPathForId, C_VNF_INSTANCE_OCC_ID, failMessage);

        //VM-LCM
        url = SUT_URL + port + "/" + VNF_LCM_OPERATIONS_URI + "/54321VMLCM";
        responseEntity = doGetRestCall(url);
        failMessage = String.format("Failed to get expected occurrence details from VM_LCM url: %s\nDetail: %s", url, responseEntity.getBody());
        assertThat(responseEntity.getStatusCode())
            .withFailMessage(failMessage)
            .isEqualTo(HttpStatusCode.valueOf(200));
        checkResponseAtJsonPath(responseEntity, jsonPathForId, VM_VNF_INSTANCE_OCC_ID, failMessage);
    }

    @Disabled("doesn't work")
    @Test
    public void deleteVnfIdentifier() {
        //C-LCM
        String vnfInstanceId = "54321";
        String url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + vnfInstanceId;
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(url, HttpMethod.DELETE, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage("HTTP response code was expected to be 204 but was %s", responseEntity.getStatusCode())
            .isEqualTo(HttpStatusCode.valueOf(204));

        //VM-LCM
        vnfInstanceId = "54321VMLCM";
        url = SUT_URL + port + "/" + VNF_INSTANCES_URI + "/" + vnfInstanceId;
        responseEntity = returnResponseEntityWithLogs(url, HttpMethod.DELETE, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode())
            .withFailMessage("HTTP response code was expected to be 204 but was %s", responseEntity.getStatusCode())
            .isEqualTo(HttpStatusCode.valueOf(204));
    }

    private ResponseEntity<String> doGetRestCall(String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(APPLICATION_JSON);   //This will come out when API is fixed (should not be adding ContentType: ApplicationJson)
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        return returnResponseEntityWithLogs(url, HttpMethod.GET, requestEntity, String.class);
    }

    public static ResponseEntity<String> returnResponseEntityWithLogs(String url, HttpMethod httpMethod, HttpEntity<?> httpEntity,
                                                                      Class<String> responseClass) {
        return new RestTemplate().exchange(url, httpMethod, httpEntity, responseClass);
    }

    private static void checkResponseForValues(ResponseEntity<String> responseEntity, String key, List<String> expectedValues, String failMessage) {
        JsonNode jsonNode = getJsonNode(responseEntity);
        List<String> actualValues = jsonNode.findValuesAsText(key);
        expectedValues.forEach(n -> assertThat(actualValues)
            .withFailMessage(failMessage)
            .contains(n));
    }

    private static void checkResponseAtJsonPath(ResponseEntity<String> responseEntity, String key, String expectedValue, String failMessage) {
        JsonNode jsonNode = getJsonNode(responseEntity);
        String actualValues = jsonNode.at(key).asText();
        assertThat(actualValues)
            .withFailMessage(failMessage)
            .contains(expectedValue);
    }

    private static JsonNode getJsonNode(final ResponseEntity<String> responseEntity) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(Objects.requireNonNull(responseEntity.getBody()));
        } catch (JsonProcessingException e) {
            fail("Error parsing response body");
        }
        return jsonNode;
    }
}
