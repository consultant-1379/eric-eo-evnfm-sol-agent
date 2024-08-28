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
package contracts.api.getAllVnfLcmOpOcc.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario for getting all VNF LCM Operation Occurrences

```
given:
  client requests all VNF LCM Operation Occurrences
when:
  a request is submitted
then:
  the request is accepted
```

""")
    request {
        method GET()
        url "/vnflcm/v1/vnf_lcm_op_occs"
        headers {
            accept(applicationJson())
        }
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body (file("AllVnfLcmOpOccDetails.json"))
        bodyMatchers {
            jsonPath('$.[0].stateEnteredTime', byCommand("assertThat(parsedJson.read(\"\$.[0].stateEnteredTime\", String.class)).isNotNull()"))
            jsonPath('$.[0].startTime', byCommand("assertThat(parsedJson.read(\"\$.[0].startTime\", String.class)).isNotNull()"))
            jsonPath('$.[0]._links', byCommand("assertThat(parsedJson.read(\"\$.[0]._links\", Object.class)).isNotNull()"))
            jsonPath('$.[0]._links.self', byCommand("assertThat(parsedJson.read(\"\$.[0]._links.self\", Object.class)).isNotNull()"))
            jsonPath('$.[0]._links.self.href', byCommand("assertThat(parsedJson.read(\"\$.[0]._links.self.href\", String.class)).isNotNull()"))
            jsonPath('$.[0]._links.instantiate', byCommand("assertThat(parsedJson.read(\"\$.[0]._links.self\", Object.class)).isNotNull()"))
            jsonPath('$.[0]._links.instantiate.href', byCommand("assertThat(parsedJson.read(\"\$.[0]._links.self.href\", String.class)).isNotNull()"))
        }
    }
}

