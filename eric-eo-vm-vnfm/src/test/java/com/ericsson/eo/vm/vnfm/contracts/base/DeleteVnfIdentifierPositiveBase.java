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

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.ericsson.eo.vm.vnfm.controller.VnfInstancesControllerImpl;
import com.ericsson.eo.vm.vnfm.service.VnfInstanceService;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeleteVnfIdentifierPositiveBase {

    @Mock
    VnfInstanceService vnfInstanceService;

    @InjectMocks
    VnfInstancesControllerImpl vnfInstanceController;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(vnfInstanceController);
    }

}
