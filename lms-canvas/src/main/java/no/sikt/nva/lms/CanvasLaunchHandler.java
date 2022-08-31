package no.sikt.nva.lms;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import sikt.lti.tp.aws.ApiGatewayLambdaLaunchHandler;

public class CanvasLaunchHandler extends ApiGatewayHandler<Void, String> {

    private static final URI DLR_BASE_URL = URI.create("https://dlr.unit.no");
    private static final URI API_HOST = URI.create("https://api.loke.aws.unit.no");
    private final Set<String> knownConsumerKeys = new ConcurrentSkipListSet<>();

    public CanvasLaunchHandler() {
        super(Void.class);
    }

    @Override
    protected String processInput(Void input, RequestInfo requestInfo, Context context) {
        // todo: add knownConsumerKeys i secrets? should be uploaded on runtime

        knownConsumerKeys.add("key");
        ApiGatewayLambdaLaunchHandler handler = new ApiGatewayLambdaLaunchHandler(API_HOST, DLR_BASE_URL,
                                                                                  knownConsumerKeys, requestInfo);
        var launchResult = handler.execute();
        String result;

        if (launchResult.getStatus() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            addAdditionalHeaders(() -> Collections.singletonMap("Location", launchResult.getLocation()));
            result = "";
        } else {
            addAdditionalHeaders(() -> Collections.singletonMap("Content-Type", launchResult.getContentType()));
            result = launchResult.getBody();
        }
        return result;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, String output) {
        return HttpURLConnection.HTTP_OK;
    }
}
