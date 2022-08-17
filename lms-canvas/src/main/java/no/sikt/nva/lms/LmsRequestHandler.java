package no.sikt.nva.lms;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LmsRequestHandler extends ApiGatewayHandler {

    private static final Logger logger = LoggerFactory.getLogger(LmsRequestHandler.class);


    public LmsRequestHandler(Class iclass, Environment environment) {
        super(iclass, environment);
    }

    @Override
    protected String processInput(Object input, RequestInfo requestInfo, Context context) {
        return "Hello world";
    }

    @Override
    protected Integer getSuccessStatusCode(Object input, Object output) {
        return HttpURLConnection.HTTP_OK;
    }


}
