package no.sikt.nva.lms;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public class LaunchException extends ApiGatewayException {

    private final Integer responseStatusCode;

    public LaunchException(String message, Integer responseStatusCode) {
        super(message);
        this.responseStatusCode = responseStatusCode;
    }

    @Override
    protected Integer statusCode() {
        return responseStatusCode;
    }
}
