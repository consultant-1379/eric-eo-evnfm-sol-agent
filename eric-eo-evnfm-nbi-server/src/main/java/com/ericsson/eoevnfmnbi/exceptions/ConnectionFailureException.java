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
package com.ericsson.eoevnfmnbi.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

public class ConnectionFailureException extends DefaultCustomException {

    @Serial
    private static final long serialVersionUID = -3078452398954998434L;

    public ConnectionFailureException(String message) {
        super("Connection Failure Exception", message, HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.value());
    }
}
