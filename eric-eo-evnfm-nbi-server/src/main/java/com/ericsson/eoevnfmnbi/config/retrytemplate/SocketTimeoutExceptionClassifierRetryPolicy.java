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
package com.ericsson.eoevnfmnbi.config.retrytemplate;

import static com.ericsson.eoevnfmnbi.utils.FilterUtils.isRetryableException;

import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

public class SocketTimeoutExceptionClassifierRetryPolicy extends ExceptionClassifierRetryPolicy {
    private static final long serialVersionUID = 1;

    public SocketTimeoutExceptionClassifierRetryPolicy(int maxAttempts) {
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(maxAttempts);

        this.setExceptionClassifier(classifiable -> { // NOSONAR
            if (isRetryableException(classifiable)) {
                return simpleRetryPolicy;
            }

            return new NeverRetryPolicy();
        });
    }
}
