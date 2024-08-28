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
package contracts.api.getVnfLcmOpOccById.positive;

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario of returning the VNF LCM Operation by OppOccId

```
given:
  client requests to get VNF LCM Operation
when:
  a valid LCM operation occurrence Id is passed
then:
  the VNF LCM Operation for that occurrence Id is returned
```

""")
    request {
        method 'GET'
        url "/vnflcm/v1/vnf_lcm_op_occs/${value(consumer(anyNonEmptyString()))}"
        headers {
            accept(applicationJson())
        }
    }
    response {
        status OK()
        body(
                """
                              {
                                "id":"dd018d7e-524c-11ea-ae59-b61d40103d36",
                                "operationState":"STARTING",
                                "vnfInstanceId":"be6d40bc-524a-11ea-ae59-b61d40103d36",
                                "grantId":"afe8fbef-0abe-4427-a00e-515aa3e6a2f3",
                                "stateEnteredTime":"2020-02-18T12:48:07Z",
                                "startTime":"2020-02-18T12:47:46Z",
                                "operation":"INSTANTIATE",
                                "requestSourceType":"NBI",
                                "operationParams":{
                                  "extVirtualLinks":[
                            
                                  ],
                                  "additionalParams":{
                                    "dataWorkerNodesSpecific":{
                                      "requiresCompensation":"true",
                                      "stackRollback":"true",
                                      "ingressNodesLimit":2,
                                      "node_pools":[
                                        {
                                          "name":"ingressnd",
                                          "image":"eccd261_node",
                                          "flavor":"c12a1_worker",
                                          "count":1,
                                          "root_volume_size":10,
                                          "labels":[
                                            "pool_type=ingress"
                                          ],
                                          "external_networks":{
                                            "undefined":true
                                          }
                                        },
                                        {
                                          "name":"workernd",
                                          "image":"eccd261_node",
                                          "flavor":"c12a1_worker",
                                          "count":1,
                                          "root_volume_size":10,
                                          "external_networks":{
                                            "undefined":true
                                          }
                                        }
                                      ],
                                      "logger_enabled":"False",
                                      "nova_availability_zone":"nova",
                                      "cinder_availability_zone":"nova"
                                    },
                                    "dataVNFSpecific":{
                                      "namespace":"test"
                                    }
                                  },
                                  "flavourId":"cee",
                                  "vimConnectionInfo":[
                                    {
                                      "id":"47772c22-7c12-49ed-8a4f-e7625b3026fb",
                                      "vimId":"vim12a1",
                                      "vimType":"OPENSTACK",
                                      "interfaceInfo":{
                                        "identityEndPoint":"https://cloud12a.athtem.eei.ericsson.se:13000/v3"
                                      },
                                      "accessInfo":{
                                        "projectId":"83743611cdf648fbb334a3b5aa9dbb3a",
                                        "credentials":{
                                          "username":"ORCH_VNF_Flash_C12A1_admin",
                                          "password":"YWRtaW4xMjM="
                                        }
                                      },
                                      "extra":{
                            
                                      }
                                    }
                                  ],
                                  "instantiationLevelId":null
                                },
                                "cancelPending":false,
                                "_links":{
                                  "self":{
                                    "href":"https://localhost/vnflcm/v1/vnf_lcm_op_occs/dd018d7e-524c-11ea-ae59-b61d40103d36"
                                  },
                                  "instantiate":{
                                    "href":"https://localhost/vnflcm/v1/vnf_instances/be6d40bc-524a-11ea-ae59-b61d40103d36"
                                  }
                                },
                                "automaticInvocation":false
                              }
                """

        )
        bodyMatchers {
            jsonPath('$.id', byCommand("assertThat(parsedJson.read(\"\$.id\", String.class)).isNotNull()"))
            jsonPath('$.operationState', byRegex("STARTING|PROCESSING|COMPLETED|FAILED_TEMP|FAILED|ROLLING_BACK|ROLLED_BACK"))
            jsonPath('$.stateEnteredTime', byCommand("assertThat(parsedJson.read(\"\$.stateEnteredTime\", String.class)).isNotNull()"))
            jsonPath('$.startTime', byCommand("assertThat(parsedJson.read(\"\$.startTime\", String.class)).isNotNull()"))
            jsonPath('$.vnfInstanceId', byCommand("assertThat(parsedJson.read(\"\$.vnfInstanceId\", String.class)).isNotNull()"))
            jsonPath('$.grantId', byCommand("assertThat(parsedJson.read(\"\$.grantId\", String.class)).isNotNull()"))
            jsonPath('$.operationParams', byCommand("assertThat(parsedJson.read(\"\$.operationParams\", Object.class)).isNotNull()"))
            jsonPath('$.operation', byRegex("INSTANTIATE|SCALE|SCALE_TO_LEVEL|CHANGE_FLAVOUR|TERMINATE|HEAL|OPERATE|CHANGE_EXT_CONN|MODIFY_INFO"))
            jsonPath('$.automaticInvocation', byRegex("true|false"))
            jsonPath('$.cancelPending', byRegex("true|false"))
            jsonPath('$._links', byCommand("assertThat(parsedJson.read(\"\$._links\", Object.class)).isNotNull()"))
            jsonPath('$._links.self', byCommand("assertThat(parsedJson.read(\"\$._links.self\", Object.class)).isNotNull()"))
            jsonPath('$._links.self.href', byCommand("assertThat(parsedJson.read(\"\$._links.self.href\", String.class)).isNotNull()"))
            jsonPath('$._links.instantiate', byCommand("assertThat(parsedJson.read(\"\$._links.instantiate\", Object.class)).isNotNull()"))
            jsonPath('$._links.instantiate.href', byCommand("assertThat(parsedJson.read(\"\$._links.instantiate.href\", String.class)).isNotNull()"))
        }
        headers {
            contentType(applicationJson())
        }
    }
}
