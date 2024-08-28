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
Represents a successful scenario for getting all VNF Instances

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
        url "/vnflcm/v1/vnf_instances"
        headers {
            accept(applicationJson())
        }
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }

        body (file("AllVnfResponseDetails.json"))
        bodyMatchers {
            jsonPath('$.[0].id', byCommand("assertThat(parsedJson.read(\"\$.[0].id\", String.class)).isNotNull()"))
            jsonPath('$.[0].vnfInstanceName', byCommand("assertThat(parsedJson.read(\"\$.[0].vnfInstanceName\", String.class)).isNotNull()"))
            jsonPath('$.[0].vnfInstanceDescription', byCommand("assertThat(parsedJson.read(\"\$[0].vnfInstanceDescription\", String.class)).isNotNull()"))
            jsonPath('$.[0].vnfdId', byCommand("assertThat(parsedJson.read(\"\$.[0].vnfdId\", String.class)).isNotNull()"))
            jsonPath('$.[0].vnfProvider', byCommand("assertThat(parsedJson.read(\"\$.[0].vnfProvider\", String.class)).isNotNull()"))
            jsonPath('$.[0].vnfProductName', byCommand("assertThat(parsedJson.read(\"\$.[0].vnfProductName\", String.class)).isNotNull()"))
            jsonPath('$.[0].vnfSoftwareVersion', byCommand("assertThat(parsedJson.read(\"\$.[0].vnfSoftwareVersion\", String.class)).isNotNull()"))
            jsonPath('$.[0].vnfdVersion', byCommand("assertThat(parsedJson.read(\"\$.[0].vnfdVersion\", String.class)).isNotNull()"))
            jsonPath('$.[0].onboardedVnfPkgInfoId', byCommand("assertThat(parsedJson.read(\"\$.[0].onboardedVnfPkgInfoId\", String.class)).isNotNull()"))
            jsonPath('$.[0].instantiationState', byRegex("NOT_INSTANTIATED|INSTANTIATED"))
        }


    }
}
