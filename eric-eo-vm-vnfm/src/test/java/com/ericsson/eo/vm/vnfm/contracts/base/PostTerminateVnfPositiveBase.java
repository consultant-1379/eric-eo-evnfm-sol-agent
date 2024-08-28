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
package com.ericsson.eo.vm.vnfm.contracts.base;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.ericsson.eo.vm.vnfm.controller.VnfInstancesControllerImpl;
import com.ericsson.eo.vm.vnfm.model.TerminateVnfRequest;
import com.ericsson.eo.vm.vnfm.service.LifeCycleManagementService;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PostTerminateVnfPositiveBase {

    @Mock
    private LifeCycleManagementService lifeCycleManagementService;

    @InjectMocks
    private VnfInstancesControllerImpl vnfInstancesController;


    @BeforeEach
    public void setUp() {
        given(lifeCycleManagementService.terminate(anyString(), any(TerminateVnfRequest.class))).willReturn("ded2e84a-521b-11ea-ae59-b61d40103d36");
        RestAssuredMockMvc.standaloneSetup(vnfInstancesController);
    }

}
