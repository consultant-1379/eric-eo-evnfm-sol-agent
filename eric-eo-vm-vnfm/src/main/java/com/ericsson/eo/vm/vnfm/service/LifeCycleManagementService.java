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

import com.ericsson.eo.vm.vnfm.model.ChangeFlavourVnfRequest;
import com.ericsson.eo.vm.vnfm.model.InstantiateVnfRequest;
import com.ericsson.eo.vm.vnfm.model.TerminateVnfRequest;

/**
 * This class prepares to make the LCM operation request and updates the DB afterwards
 */
public interface LifeCycleManagementService {

    /**
     * Prepare to make an instantiate request. There are a number of prerequisites:
     * * the vnfInstanceId exists
     * * the vnfInstance is not in the instantiated state
     *
     * @param vnfInstanceId
     * @param instantiateVnfRequest
     *
     * @return the occurrence id of this life cycle operation
     */
    String instantiate(String vnfInstanceId, InstantiateVnfRequest instantiateVnfRequest);

    /**
     * Prepare to make an instantiate request with a values file. There are a number of prerequisites:
     * * the vnfInstanceId exists
     * * the vnfInstance is not in the instantiated state
     *
     * @param vnfInstanceId
     * @param instantiateVnfRequest
     * @param toValuesFile
     *
     * @return the occurrence id of this life cycle operation
     */
    String instantiate(String vnfInstanceId, InstantiateVnfRequest instantiateVnfRequest, Path toValuesFile);

    /**
     * Prepare to make a terminate request. There are a number of prerequisites:
     * * the vnfInstanceId exists
     * * the vnfInstance is in the instantiated state
     *
     * @param vnfInstanceId
     * @param terminateVnfRequest
     *
     * @return the occurrence id of this life cycle operation
     */
    String terminate(String vnfInstanceId, TerminateVnfRequest terminateVnfRequest);


    /**
     * Prepare to make a changePackageInfo request. There are a number of prerequisites:
     * * the vnfInstanceId exists
     * * the vnfInstance is in the instantiated state
     * * the vnfdId exists
     *
     * @param vnfInstanceId
     * @param changeFlavourVnfRequest
     *
     * @return the occurrence id of this life cycle operation
     */
    String changePackageInfo(String vnfInstanceId, ChangeFlavourVnfRequest changeFlavourVnfRequest);

    /**
     * Prepare to make a changePackageInfo request. There are a number of prerequisites:
     * * the vnfInstanceId exists
     * * the vnfInstance is in the instantiated state
     * * the vnfdId exists
     * @param vnfInstanceId
     * @param changeFlavourVnfRequest
     * @param toValuesFile
     * @return
     */
    String changePackageInfo(String vnfInstanceId, ChangeFlavourVnfRequest changeFlavourVnfRequest, Path toValuesFile);

    /**
     * Checks if any progressing life-cycle for the provided instanceId
     *
     * @param instanceId
     * @return the Id of the lifecycle operation in progress for the provided instance id
     */
    String getLifeCycleInProgressForInstance(String instanceId);
}
