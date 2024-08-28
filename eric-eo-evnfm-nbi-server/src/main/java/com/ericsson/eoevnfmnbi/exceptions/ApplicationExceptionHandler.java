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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import static com.ericsson.eoevnfmnbi.utils.Constants.DYNAMIC_ERROR_RESPONSE_PARAMS;
import static com.ericsson.eoevnfmnbi.utils.Constants.GATEWAY_TIMEOUT_EXCEPTION_MESSAGE;
import static com.ericsson.eoevnfmnbi.utils.Constants.SERVICE_UNAVAILABLE_ERROR_RESPONSE_PARAMS;
import static com.ericsson.eoevnfmnbi.utils.Constants.STATUS_RESPONSE_PARAM_KEY;
import static com.ericsson.eoevnfmnbi.utils.Constants.TIMEOUT_ERROR_RESPONSE_PARAMS;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.ericsson.eoevnfmnbi.config.OnboardingConfig;

import reactor.core.publisher.Mono;

public class ApplicationExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final String USED_HOST = "org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl";

    @Value("${hosts.primary}")
    private String cvnfmOrchestratorHost;

    @Autowired
    private OnboardingConfig onboardingConfig;

    public ApplicationExceptionHandler(ErrorAttributes errorAttributes, Resources resourceProperties, ErrorProperties errorProperties,
                                       ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(final ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = super.getError(request);
        if (error instanceof DefaultCustomException) {
            DefaultCustomException exception = (DefaultCustomException) error;
            return exception.getErrorAttributes();
        } else if (error.getMessage().contains(GATEWAY_TIMEOUT_EXCEPTION_MESSAGE)) {
            return TIMEOUT_ERROR_RESPONSE_PARAMS;
        }

        DefaultCustomException defaultCustomException = new DefaultCustomException(error.getMessage());
        return defaultCustomException.getErrorAttributes();
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected int getHttpStatus(final Map<String, Object> errorAttributes) {
        return errorAttributes.get(STATUS_RESPONSE_PARAM_KEY) != null
                ? (int) errorAttributes.get(STATUS_RESPONSE_PARAM_KEY)
                : INTERNAL_SERVER_ERROR.value();
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = super.getError(request);

        if (error instanceof ConnectException) {
            Map<String, Object> errorResponseBody = buildErrorResponseBody(SERVICE_UNAVAILABLE_ERROR_RESPONSE_PARAMS,
                                                                           request);
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(errorResponseBody));
        } else if (error.getMessage() != null && error.getMessage().contains(GATEWAY_TIMEOUT_EXCEPTION_MESSAGE)) {
            Map<String, Object> errorResponseBody = buildErrorResponseBody(TIMEOUT_ERROR_RESPONSE_PARAMS, request);
            return ServerResponse.status(HttpStatus.GATEWAY_TIMEOUT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(errorResponseBody));
        } else if (error instanceof WebClientResponseException) {
            String errorResponseBody = ((WebClientResponseException) error).getResponseBodyAsString();
            HttpStatusCode statusCode = ((WebClientResponseException) error).getStatusCode();
            return ServerResponse.status(statusCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(errorResponseBody));
        }

        return super.renderErrorResponse(request);
    }

    private Map<String, Object> buildErrorResponseBody(Map<String, Object> staticParams, ServerRequest request) {
        Map<String, Object> errorParameters = new HashMap<>(staticParams);
        String requestedServiceName = resolveRequestedServiceName(request);

        for (Map.Entry<String, Object> paramEntry : errorParameters.entrySet()) {
            if (DYNAMIC_ERROR_RESPONSE_PARAMS.contains(paramEntry.getKey())) {
                paramEntry.setValue(String.format((String) paramEntry.getValue(), requestedServiceName));
            }
        }

        return errorParameters;
    }

    private String resolveRequestedServiceName(ServerRequest request) {
        if (request.attributes().get(USED_HOST).toString().contains(cvnfmOrchestratorHost)) {
            return "CVNFM Orchestrator";
        } else if (request.attributes().get(USED_HOST).toString().contains(onboardingConfig.getHost())) {
            return "Onboarding";
        } else {
            return "VM VNFM Orchestrator";
        }
    }
}
