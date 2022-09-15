package sikt.lti.tp.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static sikt.lti.tp.LtiLaunchParameter.LTI_MESSAGE_TYPE;
import static sikt.lti.tp.LtiLaunchParameter.LTI_VERSION;
import static sikt.lti.tp.LtiLaunchParameter.OAUTH_CONSUMER_KEY;
import static sikt.lti.tp.LtiLaunchParameter.RESOURCE_LINK_ID;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;
import sikt.lti.tp.LtiLaunchHandler;
import sikt.lti.tp.LtiLaunchResult;
import sikt.lti.tp.ServiceIdentifier;

public class ApiGatewayLambdaLaunchHandlerTest {

    private static final String DUMMY_VALUE = "dummy";
    private static final String SUPPORTED_LTI_MESSAGE_TYPE = "basic-lti-launch-request";
    private static final String SUPPORTED_LTI_VERSION = "LTI-1p0";
    private static final String MISSING_PARAM_MESSAGE_FORMAT = "Missing required param '%s'";
    private static final String NOT_SUPPORTED_PARAM_VALUE_FORMAT = "Only supports value '%s' for param '%s'";
    private static final String CONSUMER_KEY = "key";

    @Test
    public void shouldReturnDefaultCartridgeWhenServiceIsCombinedAndNoQueryParametersIsPresent() {
        final Map<String, String> queryStringParameters = Collections.emptyMap();
        final String serviceIdentifier = ServiceIdentifier.combined.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, queryStringParameters);

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = IoUtils.stringFromResources(Path.of("combined-cartridge-basiclti-link.xml"));

        assertThat(result.getStatus(), is(200));
        assertThat(result.getContentType(), is("application/xml; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnListOfAvailableLtiServicesOnInvalidServiceIdentifierOnUrl() {
        final Map<String, String> queryStringParameters = Collections.emptyMap();
        final String serviceIdentifier = "invalid";

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, queryStringParameters);

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = IoUtils.stringFromResources(Path.of("listOfLtiServices.html"));

        assertThat(result.getStatus(), is(200));
        assertThat(result.getContentType(), is("text/html; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnErrorOnLaunchRequestMissingOauthConsumerKey() {
        final Map<String, String> queryStringParameters = new QueryStringParametersBuilder()
                                                              .withParameter(LTI_MESSAGE_TYPE.getValue(), DUMMY_VALUE)
                                                              .build();
        final String serviceIdentifier = ServiceIdentifier.site.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, queryStringParameters);

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = String.format(MISSING_PARAM_MESSAGE_FORMAT, OAUTH_CONSUMER_KEY.getValue());

        assertThat(result.getStatus(), is(401));
        assertThat(result.getContentType(), is("text/plain; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnErrorOnLaunchRequestMissingLtiMessageType() {
        final Map<String, String> queryStringParameters = new QueryStringParametersBuilder()
                                                              .withParameter(OAUTH_CONSUMER_KEY.getValue(),
                                                                             CONSUMER_KEY)
                                                              .build();
        final String serviceIdentifier = ServiceIdentifier.site.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, queryStringParameters);

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = String.format(MISSING_PARAM_MESSAGE_FORMAT, LTI_MESSAGE_TYPE.getValue());

        assertThat(result.getStatus(), is(401));
        assertThat(result.getContentType(), is("text/plain; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnErrorOnLaunchRequestMissingLtiVersion() {
        final Map<String, String> queryStringParameters = new QueryStringParametersBuilder()
                                                              .withParameter(OAUTH_CONSUMER_KEY.getValue(),
                                                                             CONSUMER_KEY)
                                                              .withParameter(LTI_MESSAGE_TYPE.getValue(), DUMMY_VALUE)
                                                              .build();
        final String serviceIdentifier = ServiceIdentifier.site.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, queryStringParameters);

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = String.format(MISSING_PARAM_MESSAGE_FORMAT, LTI_VERSION.getValue());

        assertThat(result.getStatus(), is(401));
        assertThat(result.getContentType(), is("text/plain; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnErrorOnLaunchRequestMissingResourceLinkId() {
        final Map<String, String> queryStringParameters = new QueryStringParametersBuilder()
                                                              .withParameter(OAUTH_CONSUMER_KEY.getValue(),
                                                                             CONSUMER_KEY)
                                                              .withParameter(LTI_MESSAGE_TYPE.getValue(), DUMMY_VALUE)
                                                              .withParameter(LTI_VERSION.getValue(), DUMMY_VALUE)
                                                              .build();
        final String serviceIdentifier = ServiceIdentifier.site.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, queryStringParameters);

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = String.format(MISSING_PARAM_MESSAGE_FORMAT, RESOURCE_LINK_ID.getValue());

        assertThat(result.getStatus(), is(401));
        assertThat(result.getContentType(), is("text/plain; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnErrorOnLaunchRequestWithUnsupportedLtiMessageType() {
        final QueryStringParametersBuilder builder = new QueryStringParametersBuilder();
        builder
            .withParameter(OAUTH_CONSUMER_KEY.getValue(), CONSUMER_KEY)
            .withParameter(LTI_MESSAGE_TYPE.getValue(), DUMMY_VALUE)
            .withParameter(LTI_VERSION.getValue(), SUPPORTED_LTI_VERSION)
            .withParameter(RESOURCE_LINK_ID.getValue(), DUMMY_VALUE);

        final String serviceIdentifier = ServiceIdentifier.site.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, builder.build());

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = String.format(NOT_SUPPORTED_PARAM_VALUE_FORMAT, SUPPORTED_LTI_MESSAGE_TYPE,
                                                  LTI_MESSAGE_TYPE.getValue());

        assertThat(result.getStatus(), is(401));
        assertThat(result.getContentType(), is("text/plain; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnErrorOnLaunchRequestWithUnsupportedLtiVersion() {
        final QueryStringParametersBuilder builder = new QueryStringParametersBuilder();
        builder
            .withParameter(OAUTH_CONSUMER_KEY.getValue(), CONSUMER_KEY)
            .withParameter(LTI_MESSAGE_TYPE.getValue(), SUPPORTED_LTI_MESSAGE_TYPE)
            .withParameter(LTI_VERSION.getValue(), DUMMY_VALUE)
            .withParameter(RESOURCE_LINK_ID.getValue(), DUMMY_VALUE);

        final String serviceIdentifier = ServiceIdentifier.site.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, builder.build());

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = String.format(NOT_SUPPORTED_PARAM_VALUE_FORMAT, SUPPORTED_LTI_VERSION,
                                                  LTI_VERSION.getValue());

        assertThat(result.getStatus(), is(401));
        assertThat(result.getContentType(), is("text/plain; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnErrorOnLaunchRequestOnUnknownConsumer() {
        final QueryStringParametersBuilder builder = new QueryStringParametersBuilder();
        builder
            .withParameter(OAUTH_CONSUMER_KEY.getValue(), CONSUMER_KEY)
            .withParameter(LTI_MESSAGE_TYPE.getValue(), SUPPORTED_LTI_MESSAGE_TYPE)
            .withParameter(LTI_VERSION.getValue(), SUPPORTED_LTI_VERSION)
            .withParameter(RESOURCE_LINK_ID.getValue(), DUMMY_VALUE);

        final String serviceIdentifier = ServiceIdentifier.site.name();

        final LtiLaunchHandler launchHandler = prepareTestWithEmptyConsumerMap(serviceIdentifier, builder.build());

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = "Unknown consumer";

        assertThat(result.getStatus(), is(401));
        assertThat(result.getContentType(), is("text/plain; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnHTMLOnLaunchWithParametersAndServiceIdentifierCombined() {
        final QueryStringParametersBuilder builder = new QueryStringParametersBuilder();
        builder
            .withParameter(OAUTH_CONSUMER_KEY.getValue(), CONSUMER_KEY)
            .withParameter(LTI_MESSAGE_TYPE.getValue(), SUPPORTED_LTI_MESSAGE_TYPE)
            .withParameter(LTI_VERSION.getValue(), SUPPORTED_LTI_VERSION)
            .withParameter(RESOURCE_LINK_ID.getValue(), DUMMY_VALUE);

        final String serviceIdentifier = ServiceIdentifier.combined.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, builder.build());

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = IoUtils.stringFromResources(Path.of("listOfLtiServices.html"));

        assertThat(result.getStatus(), is(200));
        assertThat(result.getContentType(), is("text/html; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnRedirectOnSuccessfulSiteLaunchRequest() {
        final QueryStringParametersBuilder builder = new QueryStringParametersBuilder();
        builder
            .withParameter(OAUTH_CONSUMER_KEY.getValue(), CONSUMER_KEY)
            .withParameter(LTI_MESSAGE_TYPE.getValue(), SUPPORTED_LTI_MESSAGE_TYPE)
            .withParameter(LTI_VERSION.getValue(), SUPPORTED_LTI_VERSION)
            .withParameter(RESOURCE_LINK_ID.getValue(), DUMMY_VALUE);

        final String serviceIdentifier = ServiceIdentifier.site.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, builder.build());

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedLocation = "https://dlr.unit.no?forceAuthentication=true&canvasIframeResize=true";

        assertThat(result.getStatus(), is(302));
        assertThat(result.getLocation(), is(expectedLocation));
    }

    @Test
    public void shouldReturnRedirectOnSuccessfulEmbedExternalToolLaunchRequest() {
        final QueryStringParametersBuilder builder = new QueryStringParametersBuilder();
        builder
            .withParameter(OAUTH_CONSUMER_KEY.getValue(), CONSUMER_KEY)
            .withParameter(LTI_MESSAGE_TYPE.getValue(), SUPPORTED_LTI_MESSAGE_TYPE)
            .withParameter(LTI_VERSION.getValue(), SUPPORTED_LTI_VERSION)
            .withParameter(RESOURCE_LINK_ID.getValue(), DUMMY_VALUE);

        final String serviceIdentifier = ServiceIdentifier.embedExternalTool.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, builder.build());

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedLocation = "https://dlr.unit.no?forceAuthentication=true&canvasShowEmbedLinkButton=true"
                                        + "&canvasIframeResize=true&canvasLaunchPresentationReturnUrl=null";

        assertThat(result.getStatus(), is(302));
        assertThat(result.getLocation(), is(expectedLocation));
    }

    @Test
    public void shouldReturnRedirectOnSuccessfulEmbedRichContentEditorLaunchRequest() {
        final QueryStringParametersBuilder builder = new QueryStringParametersBuilder();
        builder
            .withParameter(OAUTH_CONSUMER_KEY.getValue(), CONSUMER_KEY)
            .withParameter(LTI_MESSAGE_TYPE.getValue(), SUPPORTED_LTI_MESSAGE_TYPE)
            .withParameter(LTI_VERSION.getValue(), SUPPORTED_LTI_VERSION)
            .withParameter(RESOURCE_LINK_ID.getValue(), DUMMY_VALUE);

        final String serviceIdentifier = ServiceIdentifier.embedRichContentEditor.name();

        final LtiLaunchHandler launchHandler = prepareTest(serviceIdentifier, builder.build());

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedLocation = "https://dlr.unit.no?forceAuthentication=true&canvasShowEmbedButton=true"
                                        + "&canvasIframeResize=true&canvasLaunchPresentationReturnUrl=null";

        assertThat(result.getStatus(), is(302));
        assertThat(result.getLocation(), is(expectedLocation));
    }

    private LtiLaunchHandler prepareTest(final String serviceIdentifier,
                                         final Map<String, String> queryStringParameters) {

        final Set<String> knownConsumers = Collections.singleton(CONSUMER_KEY);
        return prepareTestInternal(knownConsumers, serviceIdentifier, queryStringParameters);
    }

    private LtiLaunchHandler prepareTestWithEmptyConsumerMap(final String serviceIdentifier,
                                                             final Map<String, String> queryStringParameters) {
        final Set<String> knownConsumers = Collections.emptySet();
        return prepareTestInternal(knownConsumers, serviceIdentifier, queryStringParameters);
    }

    private LtiLaunchHandler prepareTestInternal(final Set<String> knownConsumers,
                                                 final String serviceIdentifier,
                                                 final Map<String, String> queryStringParameters) {

        final URI apiHost = URI.create("https://api.loke.aws.unit.no");
        final URI dlrBaseUrl = URI.create("https://dlr.unit.no");
        final RequestInfo requestInfo = new RequestInfo();
        requestInfo.setPathParameters(Collections.singletonMap("serviceId", serviceIdentifier));
        requestInfo.setQueryParameters(queryStringParameters);

        return new ApiGatewayLambdaLaunchHandler(apiHost, dlrBaseUrl, knownConsumers, requestInfo);
    }
}
