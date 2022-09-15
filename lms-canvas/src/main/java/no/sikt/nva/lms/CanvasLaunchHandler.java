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
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import nva.commons.secrets.SecretsReader;
import sikt.lti.tp.LtiLaunchResult;
import sikt.lti.tp.aws.ApiGatewayLambdaLaunchHandler;

public class CanvasLaunchHandler extends ApiGatewayHandler<Void, String> {


    /* default */ static final String DLR_APPLICATION_DOMAIN_ENV_NAME = "DlrApplicationDomain";
    /* default */ static final String NVA_APPLICATION_DOMAIN_ENV_NAME = "ApiDomain";
    private static final String HTTPS_SCHEME = UriWrapper.HTTPS + "://";
    private final Set<String> knownConsumerKeys = new ConcurrentSkipListSet<>();
    private final URI frontendBaseUrl;
    private final URI apiBaseUrl;
    private final SecretsReader secretsReader;

    private LtiLaunchResult ltiLaunchResult;

    @JacocoGenerated
    public CanvasLaunchHandler() {
        this(new Environment(), new SecretsReader());
    }

    public CanvasLaunchHandler(Environment environment, SecretsReader secretsReader) {
        super(Void.class, environment);
        this.frontendBaseUrl = getBaseUrlFromHost(environment, DLR_APPLICATION_DOMAIN_ENV_NAME);
        this.apiBaseUrl = getBaseUrlFromHost(environment, NVA_APPLICATION_DOMAIN_ENV_NAME);
        this.secretsReader = secretsReader;
    }

    private URI getBaseUrlFromHost(Environment environment, String dlrApplicationDomainEnvName) {
        return UriWrapper.fromUri(HTTPS_SCHEME + environment.readEnv(dlrApplicationDomainEnvName)).getUri();
    }

    @Override
    protected String processInput(Void input, RequestInfo requestInfo, Context context) throws LaunchException {

        var knowConsumerKeyValue = this.secretsReader.fetchSecret("consumerKey",
                                                                  "knownConsumerKey");
        knownConsumerKeys.add(knowConsumerKeyValue);
        ApiGatewayLambdaLaunchHandler handler = new ApiGatewayLambdaLaunchHandler(apiBaseUrl, frontendBaseUrl, knownConsumerKeys,
                                                                                  requestInfo);
        ltiLaunchResult = handler.execute();
        String responseBody;
        if (ltiLaunchResult.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new LaunchException(ltiLaunchResult.getBody(), ltiLaunchResult.getStatus());
        } else if (ltiLaunchResult.getStatus() == HttpURLConnection.HTTP_MOVED_TEMP) {
            addAdditionalHeaders(() -> Collections.singletonMap("Location", ltiLaunchResult.getLocation()));
            responseBody = "";
        } else {
            addAdditionalHeaders(() -> Collections.singletonMap("Content-Type", ltiLaunchResult.getContentType()));
            responseBody = ltiLaunchResult.getBody();
        }
        return responseBody;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, String output) {
        return ltiLaunchResult.getStatus();
    }
}
