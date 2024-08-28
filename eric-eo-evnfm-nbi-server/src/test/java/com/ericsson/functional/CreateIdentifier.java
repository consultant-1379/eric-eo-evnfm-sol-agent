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
package com.ericsson.functional;

public class CreateIdentifier {

    String vnfdId;
    String vnfInstanceDescription;
    String vnfInstanceName;

    public CreateIdentifier(final String vnfdId, final String vnfInstanceDescription, final String vnfInstanceName) {
        this.vnfdId = vnfdId;
        this.vnfInstanceDescription = vnfInstanceDescription;
        this.vnfInstanceName = vnfInstanceName;
    }

    public String getVnfdId() {
        return vnfdId;
    }

    public CreateIdentifier setVnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
        return this;
    }

    public String getVnfInstanceDescription() {
        return vnfInstanceDescription;
    }

    public CreateIdentifier setVnfInstanceDescription(final String vnfInstanceDescription) {
        this.vnfInstanceDescription = vnfInstanceDescription;
        return this;
    }

    public String getVnfInstanceName() {
        return vnfInstanceName;
    }

    public CreateIdentifier setVnfInstanceName(final String vnfInstanceName) {
        this.vnfInstanceName = vnfInstanceName;
        return this;
    }
}
