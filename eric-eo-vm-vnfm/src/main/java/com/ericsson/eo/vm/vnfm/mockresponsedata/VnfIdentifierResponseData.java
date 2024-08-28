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

import com.ericsson.eo.vm.vnfm.model.VnfInstanceResponse;

public final class VnfIdentifierResponseData {

    private VnfIdentifierResponseData() {

    }

    public static VnfInstanceResponse getVnfIdentifierResponse() {
        VnfInstanceResponse vnfIdentifierResponse = new VnfInstanceResponse();
        vnfIdentifierResponse.setId("be6d40bc-524a-11ea-ae59-b61d401074e4");
        vnfIdentifierResponse.setVnfInstanceName("checkpkg2");
        vnfIdentifierResponse.setVnfInstanceDescription("Testing_DND1");
        vnfIdentifierResponse.setVnfdId("def1ce-4cf4-477c-aab3-2b04e6a381");
        vnfIdentifierResponse.setVnfProvider("Ericsson");
        vnfIdentifierResponse.setVnfProductName("def1ce-4cf4-477c-aab3-2b04e6a381");
        vnfIdentifierResponse.setVnfSoftwareVersion("0.13.2");
        vnfIdentifierResponse.setVnfdVersion("1.0");
        vnfIdentifierResponse.setOnboardedVnfPkgInfoId("Orvnf-PKG3");
        vnfIdentifierResponse.setInstantiationState(VnfInstanceResponse.InstantiationStateEnum.NOT_INSTANTIATED);

        return vnfIdentifierResponse;
    }
}
