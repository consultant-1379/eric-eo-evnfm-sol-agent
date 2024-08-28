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
package contracts.api.getInstance.positive

import org.springframework.cloud.contract.spec.Contract

/*******************************************************************************
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
 ******************************************************************************/
Contract.make {
    description("""
Represents a successful scenario for getting a VNF Instance

```
given:
  client requests all vnf instance details
when:
  a request with is submitted
then:
  the request is accepted
```

""")
    request {
        method GET()
        url "/vnflcm/v1/vnf_instances/${value(consumer(regex(/[a-z0-9]([-a-z0-9]*[a-z0-9])?([a-z0-9]([-a-z0-9]*[a-z0-9])?)*VMLCM/)))}"
        headers {
            accept(applicationJson())
        }
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }

        body (file("SampleVnfResponse.json").asString().replaceAll("ID_TEMP", "${fromRequest().path(3).serverValue}"))
        bodyMatchers {
            jsonPath('$.id', byCommand("assertThat(parsedJson.read(\"\$.id\", String.class)).isNotNull()"))
            jsonPath('$.vnfInstanceName', byCommand("assertThat(parsedJson.read(\"\$.vnfInstanceName\", String.class)).isNotNull()"))
            jsonPath('$.vnfInstanceDescription', byCommand("assertThat(parsedJson.read(\"\$.vnfInstanceDescription\", String.class)).isNotNull()"))
            jsonPath('$.vnfdId', byCommand("assertThat(parsedJson.read(\"\$.vnfdId\", String.class)).isNotNull()"))
            jsonPath('$.vnfProvider', byCommand("assertThat(parsedJson.read(\"\$.vnfProvider\", String.class)).isNotNull()"))
            jsonPath('$.vnfProductName', byCommand("assertThat(parsedJson.read(\"\$.vnfProductName\", String.class)).isNotNull()"))
            jsonPath('$.vnfSoftwareVersion', byCommand("assertThat(parsedJson.read(\"\$.vnfSoftwareVersion\", String.class)).isNotNull()"))
            jsonPath('$.vnfdVersion', byCommand("assertThat(parsedJson.read(\"\$.vnfdVersion\", String.class)).isNotNull()"))
            jsonPath('$.onboardedVnfPkgInfoId', byCommand("assertThat(parsedJson.read(\"\$.onboardedVnfPkgInfoId\", String.class)).isNotNull()"))
            jsonPath('$.instantiationState', byRegex("NOT_INSTANTIATED|INSTANTIATED"))
        }
    }
}
