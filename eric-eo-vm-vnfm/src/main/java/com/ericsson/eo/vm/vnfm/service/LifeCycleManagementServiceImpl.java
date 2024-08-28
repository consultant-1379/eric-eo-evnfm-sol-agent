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

import java.nio.file.Path;

import org.springframework.stereotype.Service;

import com.ericsson.eo.vm.vnfm.model.ChangeFlavourVnfRequest;
import com.ericsson.eo.vm.vnfm.model.InstantiateVnfRequest;
import com.ericsson.eo.vm.vnfm.model.TerminateVnfRequest;

@Service
public class LifeCycleManagementServiceImpl implements LifeCycleManagementService {

    @Override
    public String instantiate(final String vnfInstanceId, final InstantiateVnfRequest instantiateVnfRequest) {
        return null;
    }

    @Override
    public String instantiate(final String vnfInstanceId, final InstantiateVnfRequest instantiateVnfRequest, final Path toValuesFile) {
        return null;
    }

    @Override
    public String terminate(final String vnfInstanceId, final TerminateVnfRequest terminateVnfRequest) {
        return null;
    }

    @Override
    public String changePackageInfo(final String vnfInstanceId, final ChangeFlavourVnfRequest changeFlavourVnfRequest) {
        return null;
    }

    @Override
    public String changePackageInfo(final String vnfInstanceId, final ChangeFlavourVnfRequest changeFlavourVnfRequest, final Path toValuesFile) {
        return null;
    }

    @Override
    public String getLifeCycleInProgressForInstance(final String instanceId) {
        return null;
    }
}
