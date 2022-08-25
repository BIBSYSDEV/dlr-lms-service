package sikt.lti.tp.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;
import sikt.lti.tp.Consumer;
import sikt.lti.tp.LtiLaunchResult;

public class ApiGatewayLambdaLaunchHandlerTest {

    @Test
    public void shouldReturnDefaultCartridgeWhenServiceIsCombinedAndNoQueryParametersIsPresent() {
        final Map<String, Consumer> consumerMap = Collections.emptyMap();

        final URI apiBaseUrl = URI.create("https://api.loke.aws.unit.no");
        final URI dlrBaseUrl = URI.create("https://dlr.unit.no");
        final APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setRawPath("lms/canvas/v1/combined");
        event.setQueryStringParameters(Collections.emptyMap());

        final ApiGatewayLambdaLaunchHandler launchHandler = new ApiGatewayLambdaLaunchHandler(apiBaseUrl,
                                                                                              dlrBaseUrl,
                                                                                              consumerMap,
                                                                                              event);

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = IoUtils.stringFromResources(Path.of("combined-cartridge-basiclti-link.xml"));

        assertThat(result.getStatus(), is(200));
        assertThat(result.getContentType(), is("application/xml; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }

    @Test
    public void shouldReturnListOfAvailableLtiServicesOnInvalidServiceIdentifierOnUrl() {
        final Map<String, Consumer> consumerMap = Collections.emptyMap();

        final URI apiHost = URI.create("https://api.loke.aws.unit.no");
        final URI dlrBaseUrl = URI.create("https://dlr.unit.no");
        final APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setRawPath("lms/canvas/v1/invalid");
        event.setQueryStringParameters(Collections.emptyMap());

        final ApiGatewayLambdaLaunchHandler launchHandler = new ApiGatewayLambdaLaunchHandler(apiHost,
                                                                                              dlrBaseUrl,
                                                                                              consumerMap,
                                                                                              event);

        final LtiLaunchResult result = launchHandler.execute();

        final String expectedBody = IoUtils.stringFromResources(Path.of("listOfLtiServices.html"));

        assertThat(result.getStatus(), is(200));
        assertThat(result.getContentType(), is("text/html; charset=utf-8"));
        assertThat(result.getBody(), is(expectedBody));
    }
}
