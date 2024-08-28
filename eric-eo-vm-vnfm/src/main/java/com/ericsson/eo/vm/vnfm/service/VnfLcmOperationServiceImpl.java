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

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ericsson.eo.vm.vnfm.model.VnfLcmOpOcc;

@Service
public class VnfLcmOperationServiceImpl implements VnfLcmOperationService {

    @Override
    public VnfLcmOpOcc getLcmOperationByOccId(final String id) {
        return null;
    }

    @Override
    public List<VnfLcmOpOcc> getAllLcmOperations() {
        return Collections.emptyList();
    }

}
