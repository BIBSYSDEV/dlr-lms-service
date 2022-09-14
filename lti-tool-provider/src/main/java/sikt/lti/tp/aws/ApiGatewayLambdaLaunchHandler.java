package sikt.lti.tp.aws;

import java.net.URI;
import java.util.Set;
import nva.commons.apigateway.RequestInfo;
import sikt.lti.tp.LtiLaunchHandler;

public class ApiGatewayLambdaLaunchHandler extends LtiLaunchHandler {

    public ApiGatewayLambdaLaunchHandler(final URI apiBaseUrl,
                                         final URI dlrBaseUrl,
                                         final Set<String> knownConsumers,
                                         final RequestInfo requestInfo) {
        super(apiBaseUrl,
              dlrBaseUrl,
              requestInfo.getPath(),
              requestInfo.getQueryParameters(),
              knownConsumers);
    }
}
