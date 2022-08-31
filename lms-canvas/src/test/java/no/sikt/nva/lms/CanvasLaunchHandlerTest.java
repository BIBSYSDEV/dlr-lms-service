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

    private static final String EXPECTED_LOCATION_FOR_EMBED_RICH_CONTENT_EDITOR =
        "https://dlr.unit.no?forceAuthentication=true&canvasShowEmbedButton"
        + "=true&canvasIframeResize=true&canvasLaunchPresentationReturnUrl=https://example.com";
    final Environment environment = mock(Environment.class);
    private CanvasLaunchHandler handler;
    private Context context;
    private ByteArrayOutputStream output;

    @BeforeEach
    public void init() {
        when(environment.readEnv(ApiGatewayHandler.ALLOWED_ORIGIN_ENV)).thenReturn("*");
        context = mock(Context.class);
        this.handler = new CanvasLaunchHandler();
        output = new ByteArrayOutputStream();
    }

    @Test
    void shouldRedirectOnSuccessfulLaunchRequest() throws IOException {
        var expectedLocation = constructTest("inputWithServiceId.json").getHeaders().get("Location");
        assertThat(expectedLocation, is(EXPECTED_LOCATION_FOR_EMBED_RICH_CONTENT_EDITOR));
    }

    @Test
    void shouldReturnHTMLOnLaunchRequestWithoutServiceIdentifier() throws IOException {
        var expectedHTML = constructTest("inputWithoutServiceId.json").getBody();
        assertThat(expectedHTML, is(IoUtils.stringFromResources(Path.of("ListOfServices.html"))));
    }

    @Test
    void shouldReturnXMLOnLaunchRequestWithCombinedServiceIdentifier() throws IOException {
        var expectedXML = constructTest("inputWithCombinedServiceId.json").getBody();
        assertThat(expectedXML, is(IoUtils.stringFromResources(Path.of("combined-cartridge-basiclti-link.xml"))));
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
