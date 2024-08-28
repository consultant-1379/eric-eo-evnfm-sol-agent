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
package contracts.api.postVnfIdentifier.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario of Creating a VNF Identifier

```
given:
  client requests to create a VNF Identifier
when:
  a valid request is submitted
then:
  the vnf Identifier is created
```

""")
    request {
        method 'POST'
        url '/vnflcm/v1/vnf_instances'
        body(
                "vnfdId": "g08fcbc8-474f-4673-91ee-761fd83874p9",
                "vnfInstanceName": "vnf-instance",
                "vnfInstanceDescription": "Sample description about the vnf. Another description about the vnf."
        )
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        bodyMatchers {
            jsonPath('$.vnfdId', byRegex(nonEmpty()).asString())
            jsonPath('$.vnfInstanceName', byRegex(/[a-z]([-a-z0-9]*[a-z0-9])?/))
            jsonPath('$.vnfInstanceDescription', byRegex(/(.|\s)*/))
        }
    }
    response {
        status CREATED()
        body(
                """
                          {
                            "id": "be6d40bc-524a-11ea-ae59-b61d401074e4",
                            "vnfInstanceName": "checkpkg2",
                            "vnfInstanceDescription": "Testing_DND1",
                            "vnfdId": "def1ce-4cf4-477c-aab3-2b04e6a381",
                            "vnfProvider": "Ericsson",
                            "vnfProductName": "def1ce-4cf4-477c-aab3-2b04e6a381",
                            "vnfSoftwareVersion": "0.13.2",
                            "vnfdVersion": "1.0",
                            "onboardedVnfPkgInfoId": "Orvnf-PKG3",
                            "instantiationState": "NOT_INSTANTIATED"
                          }
                  
                """
        )
        bodyMatchers {
            jsonPath('$.id', byCommand("assertThat(parsedJson.read(\"\$.id\", String.class)).isNotNull()"))
            jsonPath('$.vnfInstanceName', byRegex(/[a-z]([-a-z0-9]*[a-z0-9])?/))
            jsonPath('$.vnfInstanceDescription', byRegex(/(.|\s)*/))
            jsonPath('$.vnfdId', byCommand("assertThat(parsedJson.read(\"\$.vnfdId\", String.class)).isNotNull()"))
            jsonPath('$.vnfProvider', byCommand("assertThat(parsedJson.read(\"\$.vnfProvider\", String.class)).isNotNull()"))
            jsonPath('$.vnfProductName', byCommand("assertThat(parsedJson.read(\"\$.vnfProductName\", String.class)).isNotNull()"))
            jsonPath('$.vnfSoftwareVersion', byCommand("assertThat(parsedJson.read(\"\$.vnfSoftwareVersion\", String.class)).isNotNull()"))
            jsonPath('$.vnfdVersion', byCommand("assertThat(parsedJson.read(\"\$.vnfdVersion\", String.class)).isNotNull()"))
            jsonPath('$.onboardedVnfPkgInfoId', byCommand("assertThat(parsedJson.read(\"\$.onboardedVnfPkgInfoId\", String.class)).isNotNull()"))
            jsonPath('$.instantiationState', byRegex("NOT_INSTANTIATED|INSTANTIATED"))
        }
        headers {
            contentType(applicationJson())
        }
    }
    priority(1)
}
