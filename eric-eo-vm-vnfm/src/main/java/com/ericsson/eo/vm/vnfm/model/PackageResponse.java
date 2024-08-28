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
package com.ericsson.eo.vm.vnfm.model;

import java.util.List;

public final class PackageResponse {

    private String id;
    private String vnfdVersion;
    private String vnfSoftwareVersion;
    private String vnfProductName;
    private String vnfProvider;
    private String vnfdId;
    private String descriptorModel;
    private List<HelmPackage> helmPackageUrls;

    public String getDescriptorModel() {
        return descriptorModel;
    }

    public void setDescriptorModel(final String descriptorModel) {
        this.descriptorModel = descriptorModel;
    }

    public String getVnfdVersion() {
        return vnfdVersion;
    }

    public void setVnfdVersion(final String vnfdVersion) {
        this.vnfdVersion = vnfdVersion;
    }

    public String getVnfSoftwareVersion() {
        return vnfSoftwareVersion;
    }

    public void setVnfSoftwareVersion(final String vnfSoftwareVersion) {
        this.vnfSoftwareVersion = vnfSoftwareVersion;
    }

    public String getVnfProductName() {
        return vnfProductName;
    }

    public void setVnfProductName(final String vnfProductName) {
        this.vnfProductName = vnfProductName;
    }

    public String getVnfProvider() {
        return vnfProvider;
    }

    public void setVnfProvider(final String vnfProvider) {
        this.vnfProvider = vnfProvider;
    }

    public String getVnfdId() {
        return vnfdId;
    }

    public void setVnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<HelmPackage> getHelmPackageUrls() {
        return helmPackageUrls;
    }

    public void setHelmPackageUrls(final List<HelmPackage> helmPackageUrls) {
        this.helmPackageUrls = helmPackageUrls;
    }
}
