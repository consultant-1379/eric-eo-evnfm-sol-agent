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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.eo.vm.vnfm.annotations.DocumentController;
import com.ericsson.eo.vm.vnfm.api.VnfLcmOpOccsApi;
import com.ericsson.eo.vm.vnfm.mockresponsedata.VnfLcmOpOccResponseMockData;
import com.ericsson.eo.vm.vnfm.model.VnfLcmOpOcc;
import com.ericsson.eo.vm.vnfm.service.VnfLcmOperationServiceImpl;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
/*
Request mapping is at Controller level due to an open issue in Swagger
Code generation. A PR is open to fix it. This can be removed once the issue
is fixed
https://github.com/swagger-api/swagger-codegen/pull/8131
 */
@DocumentController
@RequestMapping("/vnflcm/v1")
@Tag(name = "vnf_lcm_op_occs")
public class VnfLcmOperationsController implements VnfLcmOpOccsApi {

    @Autowired
    VnfLcmOperationServiceImpl vnfLcmOperationServiceImpl;

    @Override
    public ResponseEntity<List<VnfLcmOpOcc>> vnfLcmOpOccsGet(String accept, String filter, String allFields, String fields, String excludeFields,
                                                             Boolean excludeDefault, String nextpageOpaqueMarker) {

        List<VnfLcmOpOcc> vnfLcmOpOccs = VnfLcmOpOccResponseMockData.getAllVnfLcmOpOccResponse();
        return new ResponseEntity<>(vnfLcmOpOccs, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccsVnfLcmOpOccIdGet(String vnfLcmOpOccId, String accept) {
        VnfLcmOpOcc vnfLcmOpOcc = VnfLcmOpOccResponseMockData.getAllVnfLcmOpOccResponse().get(0);
        return new ResponseEntity<>(vnfLcmOpOcc, HttpStatus.OK);
    }
}
