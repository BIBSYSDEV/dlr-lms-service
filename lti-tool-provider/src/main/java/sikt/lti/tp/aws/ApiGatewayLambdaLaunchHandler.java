package sikt.lti.tp.aws;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.net.URI;
import java.util.Set;
import sikt.lti.tp.LtiLaunchHandler;

public class ApiGatewayLambdaLaunchHandler extends LtiLaunchHandler {

    public ApiGatewayLambdaLaunchHandler(final URI apiBaseUrl,
                                         final URI dlrBaseUrl,
                                         final Set<String> knownConsumers,
                                         final APIGatewayV2HTTPEvent event) {
        super(apiBaseUrl, dlrBaseUrl, event.getRawPath(), event.getQueryStringParameters(), knownConsumers);
    }
}
