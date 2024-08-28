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
package com.ericsson.eo.vm.vnfm.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ericsson.eo.vm.vnfm.mockresponsedata.VnfInstanceResponseMockData;
import com.ericsson.eo.vm.vnfm.model.PackageResponse;
import com.ericsson.eo.vm.vnfm.model.VnfInstanceResponse;

@Service
public class VnfInstanceService {
    private final List<VnfInstanceResponse> vnfInstanceResponse;
    private final List<PackageResponse> packageResponse;

    public VnfInstanceService() {
        packageResponse = new ArrayList<>();
        packageResponse.add(new PackageResponse());
        vnfInstanceResponse = new ArrayList<>();
        vnfInstanceResponse.add(new VnfInstanceResponse());
    }

    public List<VnfInstanceResponse> getAllVnfs() {
        return VnfInstanceResponseMockData.getVnfInstancesResponses();
    }

    public VnfInstanceResponse getSampleVnf() {
        return VnfInstanceResponseMockData.getSampleVnf();
    }

    public PackageResponse getPackageInfoWithDescriptorModel() {
        return packageResponse.get(0);
    }

}
