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
package com.ericsson.eo.vm.vnfm.mockresponsedata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ericsson.eo.vm.vnfm.model.AccessInfo;
import com.ericsson.eo.vm.vnfm.model.AdditionalParams;
import com.ericsson.eo.vm.vnfm.model.Credentials;
import com.ericsson.eo.vm.vnfm.model.DataVNFSpecific;
import com.ericsson.eo.vm.vnfm.model.DataWorkerNodesSpecific;
import com.ericsson.eo.vm.vnfm.model.ExternalNetworks;
import com.ericsson.eo.vm.vnfm.model.InterfaceInfo;
import com.ericsson.eo.vm.vnfm.model.NodePool;
import com.ericsson.eo.vm.vnfm.model.OperationParams;
import com.ericsson.eo.vm.vnfm.model.URILink;
import com.ericsson.eo.vm.vnfm.model.VimConnectionInfo;
import com.ericsson.eo.vm.vnfm.model.VnfLcmOpOcc;
import com.ericsson.eo.vm.vnfm.model.VnfLcmOpOccLinks;

public final class VnfLcmOpOccResponseMockData {
    private static List<VnfLcmOpOcc> vnfLcmOpOccs = new ArrayList<>();

    private VnfLcmOpOccResponseMockData() {
    }

    public static List<VnfLcmOpOcc> getAllVnfLcmOpOccResponse() {
        VnfLcmOpOcc vnfLcmOpOcc = new VnfLcmOpOcc();
        vnfLcmOpOcc.setId("dd018d7e-524c-11ea-ae59-b61d40103d36");
        vnfLcmOpOcc.setVnfInstanceId("be6d40bc-524a-11ea-ae59-b61d40103d36");
        vnfLcmOpOcc.setOperationState(VnfLcmOpOcc.OperationStateEnum.STARTING);
        vnfLcmOpOcc.setStartTime(new Date());
        vnfLcmOpOcc.setStateEnteredTime(new Date());
        vnfLcmOpOcc.setGrantId("afe8fbef-0abe-4427-a00e-515aa3e6a2f3");
        vnfLcmOpOcc.setOperation(VnfLcmOpOcc.OperationEnum.INSTANTIATE);
        vnfLcmOpOcc.setRequestSourceType("NBI");
        vnfLcmOpOcc.setAutomaticInvocation(false);
        vnfLcmOpOcc.setCancelPending(false);

        OperationParams operationParams = new OperationParams();

        AdditionalParams additionalParams = new AdditionalParams();

        DataWorkerNodesSpecific dataWorkerNodesSpecific = new DataWorkerNodesSpecific();
        dataWorkerNodesSpecific.setRequiresCompensation("true");
        dataWorkerNodesSpecific.setStackRollback("true");
        dataWorkerNodesSpecific.setIngressNodesLimit(2);

        NodePool nodePool = new NodePool();
        nodePool.setName("ingressnd");
        nodePool.setImage("eccd261_node");
        nodePool.setFlavor("c12a1_worker");
        nodePool.setCount(1);
        nodePool.setRootVolumeSize(10);
        List<String> labels = new ArrayList<>();
        labels.add("pool_type=ingress");

        nodePool.setLabels(labels);

        ExternalNetworks externalNetworks = new ExternalNetworks();
        externalNetworks.setUndefined(true);
        nodePool.setExternalNetworks(externalNetworks);
        List<NodePool> nodePoolList = new ArrayList<>();
        nodePoolList.add(nodePool);

        nodePool = new NodePool();
        nodePool.setName("workernd");
        nodePool.setImage("eccd261_node");
        nodePool.setFlavor("c12a1_worker");
        nodePool.setCount(1);
        nodePool.setRootVolumeSize(10);
        externalNetworks = new ExternalNetworks();
        externalNetworks.setUndefined(true);
        nodePool.setExternalNetworks(externalNetworks);
        nodePoolList.add(nodePool);

        dataWorkerNodesSpecific.setNodePools(nodePoolList);
        dataWorkerNodesSpecific.setLoggerEnabled("False");
        dataWorkerNodesSpecific.setNovaAvailabilityZone("nova");
        dataWorkerNodesSpecific.setCinderAvailabilityZone("nova");

        DataVNFSpecific dataVNFSpecific = new DataVNFSpecific();
        dataVNFSpecific.setNamespace("test");

        additionalParams.dataWorkerNodesSpecific(dataWorkerNodesSpecific);
        additionalParams.dataVNFSpecific(dataVNFSpecific);

        operationParams.setAdditionalParams(additionalParams);
        operationParams.setFlavourId("cee");

        List<VimConnectionInfo> vimConnectionInfos = new ArrayList<>();
        VimConnectionInfo vimConnectionInfo = new VimConnectionInfo();
        vimConnectionInfo.setId("47772c22-7c12-49ed-8a4f-e7625b3026fb");
        vimConnectionInfo.setVimId("vim12a1");
        vimConnectionInfo.setVimType("OPENSTACK");

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setIdentityEndPoint("https://cloud12a.athtem.eei.ericsson.se:13000/v3");
        vimConnectionInfo.setInterfaceInfo(interfaceInfo);

        AccessInfo accessInfo = new AccessInfo();
        accessInfo.setProjectId("83743611cdf648fbb334a3b5aa9dbb3a");
        Credentials credentials = new Credentials();
        credentials.setUsername("ORCH_VNF_Flash_C12A1_admin");
        credentials.setPassword("YWRtaW4xMjM=");
        accessInfo.setCredentials(credentials);
        vimConnectionInfo.setAccessInfo(accessInfo);
        vimConnectionInfos.add(vimConnectionInfo);

        operationParams.setVimConnectionInfo(vimConnectionInfos);
        operationParams.setInstantiationLevelId(null);

        vnfLcmOpOcc.setOperationParams(operationParams);

        URILink vnfOppOccSelfLink = new URILink();
        vnfOppOccSelfLink.setHref("http://localhost/vnflcm/v1/vnf_lcm_op_occs/dd018d7e-524c-11ea-ae59-b61d40103d36");

        URILink vnfOppOccInstanceLink = new URILink();
        vnfOppOccInstanceLink.setHref("http://localhost/vnflcm/v1/vnf_instances/be6d40bc-524a-11ea-ae59-b61d40103d36");

        VnfLcmOpOccLinks vnfOppOccResponseLinks = new VnfLcmOpOccLinks();
        vnfOppOccResponseLinks.setSelf(vnfOppOccSelfLink);
        vnfOppOccResponseLinks.setInstantiate(vnfOppOccInstanceLink);
        vnfLcmOpOcc.setLinks(vnfOppOccResponseLinks);

        vnfLcmOpOccs.add(vnfLcmOpOcc);

        return vnfLcmOpOccs;
    }
}
