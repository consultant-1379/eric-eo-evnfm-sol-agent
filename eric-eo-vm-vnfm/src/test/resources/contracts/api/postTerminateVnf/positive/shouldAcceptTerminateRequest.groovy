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
package contracts.api.postTerminateVnf.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario scenario of terminating a VNF Instance

```
given:
  client requests to terminate a VNF Instance
when:
  a valid request with a vnfInstanceId is submitted
then:
  the VNF instance is terminated
```

""")
    request {
        method 'POST'
        urlPath($(regex("/vnflcm/v1/vnf_instances/[a-z0-9]([-a-z0-9]*[a-z0-9])?([a-z0-9]([-a-z0-9]*[a-z0-9])?)*VMLCM/terminate")))
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        body(
                "terminationType": "FORCEFUL"
        )
    }
    response {
        status ACCEPTED()
        headers {
            header(location(), "http://localhost/vnflcm/v1/vnf_lcm_op_occs/ded2e84a-521b-11ea-ae59-b61d40103d36")
        }
    }
    priority(3)
}
