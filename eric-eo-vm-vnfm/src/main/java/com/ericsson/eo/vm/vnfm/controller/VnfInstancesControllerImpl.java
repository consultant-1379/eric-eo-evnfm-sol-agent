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
package com.ericsson.eo.vm.vnfm.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ericsson.eo.vm.vnfm.annotations.DocumentController;
import com.ericsson.eo.vm.vnfm.api.VnfInstancesApi;
import com.ericsson.eo.vm.vnfm.mockresponsedata.VnfIdentifierResponseData;
import com.ericsson.eo.vm.vnfm.model.ChangeFlavourVnfRequest;
import com.ericsson.eo.vm.vnfm.model.ChangePackageInfoVnfRequest;
import com.ericsson.eo.vm.vnfm.model.CreateVnfRequest;
import com.ericsson.eo.vm.vnfm.model.InstantiateVnfRequest;
import com.ericsson.eo.vm.vnfm.model.TerminateVnfRequest;
import com.ericsson.eo.vm.vnfm.model.VnfInstanceResponse;
import com.ericsson.eo.vm.vnfm.service.LifeCycleManagementService;
import com.ericsson.eo.vm.vnfm.service.VnfInstanceService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@DocumentController
@RestController
@RequestMapping("/vnflcm/v1")
@Tag(name = "vnf_instances")
public class VnfInstancesControllerImpl implements VnfInstancesApi {

    @Autowired
    private VnfInstanceService instanceService;

    @Autowired
    private LifeCycleManagementService lifeCycleManagementService;

    @Override
    public ResponseEntity<List<VnfInstanceResponse>> vnfInstancesGet(String accept, String filter, String fields, String allFields,
                                                                     String excludeFields, Boolean excludeDefault, String nextpageOpaqueMarker) {

        List<VnfInstanceResponse> vnfInstanceResponses = instanceService.getAllVnfs();
        return new ResponseEntity<>(vnfInstanceResponses, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> vnfInstancesVnfInstanceIdInstantiatePost(String vnfInstanceId,
                                                                         String accept,
                                                                         String contentType,
                                                                         InstantiateVnfRequest instantiateVnfRequest) {
        String lifeCycleOperationOccurrenceId = lifeCycleManagementService.instantiate(vnfInstanceId, instantiateVnfRequest);
        HttpHeaders headers = getHttpHeaders(lifeCycleOperationOccurrenceId);
        return new ResponseEntity<>(null, headers, HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<Void> vnfInstancesVnfInstanceIdTerminatePost(String vnfInstanceId,
                                                                       String accept,
                                                                       String contentType,
                                                                       TerminateVnfRequest terminateVnfRequest) {
        String lifeCycleOperationOccurrenceId = lifeCycleManagementService.terminate(vnfInstanceId, terminateVnfRequest);
        HttpHeaders headers = getHttpHeaders(lifeCycleOperationOccurrenceId);
        return new ResponseEntity<>(null, headers, HttpStatus.ACCEPTED);
    }

    public static HttpHeaders getHttpHeaders(final String lifeCycleOperationOccurrenceId) {
        HttpHeaders headers = new HttpHeaders();
        final String host = getHostUrl();
        headers.add(HttpHeaders.LOCATION, host + "/vnflcm/v1/vnf_lcm_op_occs/" + lifeCycleOperationOccurrenceId);
        return headers;
    }

    public static String getHostUrl() {
        StringBuilder resolvedUrl = new StringBuilder();
        HttpServletRequest request = getCurrentHttpRequest();
        String protocol = StringUtils.containsIgnoreCase(request.getHeader("x-forwarded-proto"), "https") ? "https" : "http";
        String host = request.getHeader("x-forwarded-host") != null ? request.getHeader("x-forwarded-host") : request.getRemoteHost();
        resolvedUrl.append(protocol).append("://").append(host);
        return resolvedUrl.toString();
    }

    public static HttpServletRequest getCurrentHttpRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    @Override
    public ResponseEntity<VnfInstanceResponse> vnfInstancesPost(String accept,
                                                                String contentType,
                                                                CreateVnfRequest createVnfRequest) {
        return new ResponseEntity<>(VnfIdentifierResponseData.getVnfIdentifierResponse(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> vnfInstancesVnfInstanceIdChangeFlavourPost(String vnfInstanceId,
                                                                           String accept,
                                                                           String contentType,
                                                                           ChangeFlavourVnfRequest changeFlavourVnfRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> vnfInstancesVnfInstanceIdChangeVnfpkgPost(String vnfInstanceId,
                                                                          String accept,
                                                                          String contentType,
                                                                          ChangePackageInfoVnfRequest changePackageInfoVnfRequest) {
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<Void> vnfInstancesVnfInstanceIdDelete(String vnfInstanceId, String accept) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<VnfInstanceResponse> vnfInstancesVnfInstanceIdGet(String vnfInstanceId, String accept) {
        VnfInstanceResponse vnfInstanceResponses = instanceService.getSampleVnf();
        return new ResponseEntity<>(vnfInstanceResponses, HttpStatus.OK);
    }
}
