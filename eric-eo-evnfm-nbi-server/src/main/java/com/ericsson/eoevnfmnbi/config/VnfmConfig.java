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
package com.ericsson.eoevnfmnbi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnfm")
public class VnfmConfig {

    private String host;
    private VnfConfig vnf;
    private OccurrenceConfig occurrence;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public OccurrenceConfig getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(OccurrenceConfig occurrence) {
        this.occurrence = occurrence;
    }

    public VnfConfig getVnf() {
        return vnf;
    }

    public void setVnf(VnfConfig vnf) {
        this.vnf = vnf;
    }
}
