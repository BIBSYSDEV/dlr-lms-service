package no.sikt.nva.lms;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class LmsRequestHandler extends ApiGatewayHandler<Void, Void> {

    public LmsRequestHandler(Class iclass, Environment environment) {
        super(iclass, environment);
    }

    @JacocoGenerated
    @Override
    protected Void processInput(Void input, RequestInfo requestInfo, Context context) {

        return input;
    }

    @JacocoGenerated
    @Override
    protected Integer getSuccessStatusCode(Void input, Void output) {
        return HttpURLConnection.HTTP_OK;
    }
}
