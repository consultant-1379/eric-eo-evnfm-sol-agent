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
package com.ericsson.eoevnfmnbi.utils;

import java.util.Map;
import java.util.regex.Pattern;

public final class RequestMappingUtils {
    private static final String VNF_INSTANCE_REGEX = "^/vnflcm/v1/vnf_instances/[\\w+-]+";
    private static final String VNF_INSTANCE_BACKUP_REGEX = "/[\\w+/]+";
    private static final String VNF_LCM_OP_OCCS_REGEX = "^/vnflcm/v1/vnf_lcm_op_occs/[\\w+-]+";
    private static final String CLUSTER_CONFIG_REGEX = "^/vnflcm/v1/clusterconfigs/\\w+";
    private static final String NAMESPACE_REGEX = "^/vnflcm/v1/validateNamespace/[\\w+/-]+";
    private static final String RESOURCES_REGEX = "^/api/v1/resources/[\\w+-]+";

    private static final Map<Pattern, String> vnfInstanceRequestMap = Map.ofEntries(
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX),
                      "/vnflcm/v1/vnf_instances/{vnfInstanceId}"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/instantiate"),
                      "/vnflcm/v1/vnf_instances/{vnfInstanceId}/instantiate"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/change_package_info"),
                      "/vnflcm/v1/vnf_instances/{vnfInstanceId}/change_package_info"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/terminate"),
                      "/vnflcm/v1/vnf_instances/{vnfInstanceId}/terminate"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/change_vnfpkg"),
                      "/vnflcm/v1/vnf_instances/{vnfInstanceId}/change_vnfpkg"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/scale"),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/scale"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/addNode"),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/addNode"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/deleteNode"),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/deleteNode"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/cleanup"),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/cleanup"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/values"),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/values"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/backups"),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/backups"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/backups" + VNF_INSTANCE_BACKUP_REGEX),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/backups/{backupName}/{scope}"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/backup/scopes"),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/backup/scopes"),
            Map.entry(Pattern.compile(VNF_INSTANCE_REGEX + "/sync"),
                    "/vnflcm/v1/vnf_instances/{vnfInstanceId}/sync")
    );
    private static final Map<Pattern, String> vnfLcmOoOccsRequestMap = Map.ofEntries(
            Map.entry(Pattern.compile(VNF_LCM_OP_OCCS_REGEX),
                      "/vnflcm/v1/vnf_lcm_op_occs/{vnfLcmOpOccId}"),
            Map.entry(Pattern.compile(VNF_LCM_OP_OCCS_REGEX + "/rollback"),
                      "/vnflcm/v1/vnf_lcm_op_occs/{vnfLcmOpOccId}/rollback"),
            Map.entry(Pattern.compile(VNF_LCM_OP_OCCS_REGEX + "/fail"),
                      "/vnflcm/v1/vnf_lcm_op_occs/{vnfLcmOpOccId}/fail")
    );
    private static final Map<Pattern, String> clusterConfigRequestMap = Map.ofEntries(
            Map.entry(Pattern.compile(CLUSTER_CONFIG_REGEX),
                      "/vnflcm/v1/clusterconfigs/{clusterConfigName}")
    );
    private static final Map<Pattern, String> namespaceRequestMap = Map.ofEntries(
            Map.entry(Pattern.compile(NAMESPACE_REGEX),
                      "/vnflcm/v1/validateNamespace/{clusterName}/{namespace}")
    );

    private static final Map<Pattern, String> resourceRequestMap = Map.ofEntries(
            Map.entry(Pattern.compile(RESOURCES_REGEX),
                      "/api/v1/resources/{resourceId}"),
            Map.entry(Pattern.compile(RESOURCES_REGEX + "/pods"),
                      "/api/v1/resources/{resourceId}/pods"),
            Map.entry(Pattern.compile(RESOURCES_REGEX + "/vnfcScaleInfo"),
                      "/api/v1/resources/{resourceId}/vnfcScaleInfo"),
            Map.entry(Pattern.compile(RESOURCES_REGEX + "/downgradeInfo"),
                      "/api/v1/resources/{resourceId}/downgradeInfo"),
            Map.entry(Pattern.compile(RESOURCES_REGEX + "/rollbackInfo"),
                      "/api/v1/resources/{resourceId}/rollbackInfo")
    );

    private static final Map<String, Map<Pattern, String>> requestMap = Map.ofEntries(
            Map.entry("/vnflcm/v1/vnf_instances/", vnfInstanceRequestMap),
            Map.entry("/vnflcm/v1/vnf_lcm_op_occs/", vnfLcmOoOccsRequestMap),
            Map.entry("/vnflcm/v1/clusterconfigs/", clusterConfigRequestMap),
            Map.entry("/vnflcm/v1/validateNamespace/", namespaceRequestMap),
            Map.entry("/api/v1/resources/", resourceRequestMap)
    );

    private RequestMappingUtils() {
    }

    public static Map<String, Map<Pattern, String>> requestMap() {
        return requestMap;
    }
}
