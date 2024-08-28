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
package com.ericsson.eo.vm.vnfm.mockresponsedata;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.eo.vm.vnfm.model.VnfInstanceResponse;

public final class VnfInstanceResponseMockData {

    private static final List<VnfInstanceResponse> vnfInstances = new ArrayList<>();

    private VnfInstanceResponseMockData() {
    }

    public static List<VnfInstanceResponse> getVnfInstancesResponses() {
        VnfInstanceResponse vnfInstanceResponse = createVnfInstanceResponse();
        vnfInstances.add(vnfInstanceResponse);
        return vnfInstances;
    }

    public static VnfInstanceResponse getSampleVnf() {
        return createVnfInstanceResponse();
    }

    private static VnfInstanceResponse createVnfInstanceResponse() {
        VnfInstanceResponse vnfInstanceResponse = new VnfInstanceResponse();
        vnfInstanceResponse.setId("be6d40bc-524a-11ea-ae59-b61d40103d36");
        vnfInstanceResponse.setVnfInstanceName("checkpkg1");
        vnfInstanceResponse.setVnfInstanceDescription("Testing_DND");
        vnfInstanceResponse.setVnfdId("def1ce-4cf4-477c-aab3-2b04e6a381");
        vnfInstanceResponse.setVnfProvider("Ericsson");
        vnfInstanceResponse.setVnfProductName("def1ce-4cf4-477c-aab3-2b04e6a381");
        vnfInstanceResponse.setVnfSoftwareVersion("0.13.2");
        vnfInstanceResponse.setVnfdVersion("1.0");
        vnfInstanceResponse.setOnboardedVnfPkgInfoId("Orvnf-PKG3");
        vnfInstanceResponse.setInstantiationState(VnfInstanceResponse.InstantiationStateEnum.NOT_INSTANTIATED);
        return vnfInstanceResponse;
    }
}
