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
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class DefaultCustomException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3091891313415632224L;

    private final String title;
    private final String message;
    private final String type;
    private final int status;

    public DefaultCustomException(String title, String message, String type, int status) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = status;
    }

    public DefaultCustomException(String title, String message) {
        this(title, message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public DefaultCustomException(String message) {
        this("Internal Server Error", message);
    }

    public Map<String, Object> getErrorAttributes() {
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("title", this.title);
        errorAttributes.put("detail", this.message);
        errorAttributes.put("type", this.type);
        errorAttributes.put("status", this.status);

        return errorAttributes;
    }
}