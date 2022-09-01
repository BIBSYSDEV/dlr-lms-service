package no.sikt.nva.lms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sikt.lti.tp.LtiLaunchHandler;

public class CanvasLaunchHandlerTest {

    public static final String DLR_BASE_URL_VALUE = "https://dlr.unit.no";
    public static final String API_HOST_VALUE = "https://api.loke.aws.unit.no";
    private static final String EXPECTED_LOCATION_FOR_EMBED_RICH_CONTENT_EDITOR =
        "https://dlr.unit.no?forceAuthentication=true&canvasShowEmbedButton"
        + "=true&canvasIframeResize=true&canvasLaunchPresentationReturnUrl=https://example.com";
    private static final String DLR_BASE_URL = "DLR_BASE_URL";
    private static final String API_HOST = "API_HOST";
    final Environment environment = mock(Environment.class);
    private CanvasLaunchHandler handler;
    private Context context;
    private ByteArrayOutputStream output;

    @BeforeEach
    public void init() {
        when(environment.readEnv(ApiGatewayHandler.ALLOWED_ORIGIN_ENV)).thenReturn("*");
        when(environment.readEnv(DLR_BASE_URL)).thenReturn(DLR_BASE_URL_VALUE);
        when(environment.readEnv(API_HOST)).thenReturn(API_HOST_VALUE);

        context = mock(Context.class);
        this.handler = new CanvasLaunchHandler(environment);
        output = new ByteArrayOutputStream();
    }

    @Test
    void shouldRedirectOnSuccessfulLaunchRequest() throws IOException {
        var actualLocation = constructTest("inputWithServiceId.json").getHeaders().get("Location");
        assertThat(actualLocation, is(EXPECTED_LOCATION_FOR_EMBED_RICH_CONTENT_EDITOR));
    }

    @Test
    void shouldReturnHTMLOnLaunchRequestWithoutServiceIdentifier() throws IOException {
        var actualHTML = constructTest("inputWithoutServiceId.json").getBody();
        assertThat(actualHTML, is(IoUtils.stringFromResources(Path.of("listOfServices.html"))));
    }

    @Test
    void shouldReturnXMLOnLaunchRequestWithCombinedServiceIdentifier() throws IOException {
        var actualXML = constructTest("inputWithCombinedServiceId.json").getBody();
        assertThat(actualXML, is(IoUtils.stringFromResources(Path.of("combined-cartridge-basiclti-link.xml"))));
    }

    @Test
    void shouldConvertUnauthorizedLtiLaunchRequestToJsonError() throws IOException {
        var actualJsonError = constructTest("inputWithUnknownConsumerKey.json").getBody();
        assertThat(actualJsonError, is(IoUtils.stringFromResources(Path.of("unknownConsumerResponse.json"))));
    }

    @Test
    void shouldConvert500ResponseToJsonError() throws IOException {
        var actualJsonError = constructTest("inputWithoutPathParameter.json").getBody();
        assertThat(actualJsonError, is(IoUtils.stringFromResources(Path.of("500ServerResponse.json"))));
    }

    private GatewayResponse<LtiLaunchHandler> constructTest(String requestJson) throws IOException {
        var input = createRequest(requestJson);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, LtiLaunchHandler.class);
    }

    private InputStream createRequest(String string) {
        return IoUtils.inputStreamFromResources(string);
    }
}
