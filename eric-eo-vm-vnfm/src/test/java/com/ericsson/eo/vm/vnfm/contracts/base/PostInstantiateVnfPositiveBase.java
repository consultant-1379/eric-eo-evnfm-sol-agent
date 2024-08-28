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

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

import com.ericsson.eo.vm.vnfm.model.InstantiateVnfRequest;
import com.ericsson.eo.vm.vnfm.service.LifeCycleManagementService;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PostInstantiateVnfPositiveBase {

    @MockBean
    private LifeCycleManagementService lifeCycleManagementService;

    @Autowired
    private WebApplicationContext applicationContext;

    @BeforeEach
    public void setUp() {
        given(lifeCycleManagementService.instantiate(anyString(), any(InstantiateVnfRequest.class)))
                .willReturn("dd018d7e-524c-11ea-ae59-b61d40103d36");
        given(lifeCycleManagementService.instantiate(anyString(), any(InstantiateVnfRequest.class), any(Path.class)))
                .willReturn("dd018d7e-524c-11ea-ae59-b61d40103d36");
        RestAssuredMockMvc.webAppContextSetup(applicationContext);
    }

}
