package sikt.lti.tp.aws;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.net.URI;
import java.util.Map;
import sikt.lti.tp.Consumer;
import sikt.lti.tp.LtiLaunchHandler;

public class ApiGatewayLambdaLaunchHandler extends LtiLaunchHandler {

    public ApiGatewayLambdaLaunchHandler(final URI apiBaseUrl,
                                         final URI dlrBaseUrl,
                                         final Map<String, Consumer> consumerByKeyMap,
                                         final APIGatewayV2HTTPEvent event) {
        super(apiBaseUrl, dlrBaseUrl, event.getRawPath(), event.getQueryStringParameters(), consumerByKeyMap);
    }
}
