package no.sikt.nva.lms;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;
import sikt.lti.tp.aws.ApiGatewayLambdaLaunchHandler;

public class CanvasLaunchHandler extends ApiGatewayHandler<Void, String> {

    private static final String DLR_BASE_URL = "DLR_BASE_URL";
    private static final String API_HOST = "API_HOST";
    private final Set<String> knownConsumerKeys = new ConcurrentSkipListSet<>();
    private final URI baseUrl;
    private final URI apiHost;

    public CanvasLaunchHandler(Environment environment) {
        super(Void.class, environment);
        baseUrl = URI.create(environment.readEnv(DLR_BASE_URL));
        apiHost = URI.create(environment.readEnv(API_HOST));
    }

    @Override
    protected String processInput(Void input, RequestInfo requestInfo, Context context) throws LaunchException {
        // todo: add knownConsumerKeys i secrets? should be uploaded on runtime

        knownConsumerKeys.add("key");
        ApiGatewayLambdaLaunchHandler handler = new ApiGatewayLambdaLaunchHandler(apiHost, baseUrl, knownConsumerKeys,
                                                                                  requestInfo);
        var launchResult = handler.execute();
        String result;
        if (launchResult.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new LaunchException(launchResult.getBody(), launchResult.getStatus());
        } else if (launchResult.getStatus() == HttpURLConnection.HTTP_NOT_MODIFIED) {
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
