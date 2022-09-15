package no.sikt.nva.lms;

import static no.sikt.nva.lms.CanvasLaunchHandler.DLR_APPLICATION_DOMAIN_ENV_NAME;
import static no.sikt.nva.lms.CanvasLaunchHandler.NVA_APPLICATION_DOMAIN_ENV_NAME;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import no.unit.nva.stubs.FakeSecretsManagerClient;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.secrets.SecretsReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sikt.lti.tp.LtiLaunchHandler;

public class CanvasLaunchHandlerTest {

    private static final String DLR_BASE_URL_VALUE = "frontend.sandbox.dlr.aws.unit.no";
    private static final String API_HOST_VALUE = "api.sandbox.nva.aws.unit.no";
    private static final String EXPECTED_LOCATION_FOR_EMBED_RICH_CONTENT_EDITOR =
        "https://frontend.sandbox.dlr.aws.unit.no?forceAuthentication=true&canvasShowEmbedButton"
        + "=true&canvasIframeResize=true&canvasLaunchPresentationReturnUrl=https://example.com";
    private static final String SECRET_VALUE = "someSecretValue"; // same secret is used in .json test files
    final Environment environment = mock(Environment.class);
    private CanvasLaunchHandler handler;
    private Context context;
    private ByteArrayOutputStream output;
    private FakeSecretsManagerClient fakeSecretsManagerClient;

    @BeforeEach
    public void init() {
        when(environment.readEnv(ApiGatewayHandler.ALLOWED_ORIGIN_ENV)).thenReturn("*");
        when(environment.readEnv(DLR_APPLICATION_DOMAIN_ENV_NAME)).thenReturn(DLR_BASE_URL_VALUE);
        when(environment.readEnv(NVA_APPLICATION_DOMAIN_ENV_NAME)).thenReturn(API_HOST_VALUE);
        this.fakeSecretsManagerClient = new FakeSecretsManagerClient();
        fakeSecretsManagerClient.putSecret("consumerKey", "knownConsumerKey", SECRET_VALUE);
        context = mock(Context.class);
        this.handler = new CanvasLaunchHandler(environment, new SecretsReader(fakeSecretsManagerClient));
        output = new ByteArrayOutputStream();
    }

    @Test
    void shouldRedirectOnSuccessfulLaunchRequest() throws IOException {
        var response = constructTest("inputWithServiceIdAndQueryParams.json");
        var actualLocation = response.getHeaders().get("Location");
        var actualStatusCode = response.getStatusCode();
        assertThat(actualLocation, is(EXPECTED_LOCATION_FOR_EMBED_RICH_CONTENT_EDITOR));
        assertThat(actualStatusCode, is(equalTo(HttpURLConnection.HTTP_MOVED_TEMP)));
    }

    @Test
    void shouldReturnDefaultCartridgeOnLaunchRequestWithCombinedServiceIdentifierAndNoQueryParams() throws IOException {
        var response = constructTest("inputWithCombinedServiceId.json");
        var actualStatusCode = response.getStatusCode();
        var actualBody = response.getBody();
        assertThat(actualStatusCode, is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(actualBody,
                   is(IoUtils.stringFromResources(Path.of("combined-cartridge-basiclti-link.xml"))));
    }

    @Test
    void shouldConvertUnauthorizedLtiLaunchRequestToJsonError() throws IOException {
        var response = constructTest("inputWithUnknownConsumerKey.json");
        var actualStatusCode = response.getStatusCode();
        assertThat(actualStatusCode, is(equalTo(HttpURLConnection.HTTP_UNAUTHORIZED)));
        assertThat(response.getBody(), is(IoUtils.stringFromResources(Path.of("unknownConsumerResponse.json"))));
    }

    @Test
    void shouldReadSecretKeyValue() {
        var secretName = randomString();
        var secretKey = randomString();
        var secretValue = randomString();
        fakeSecretsManagerClient.putSecret(secretName, secretKey, secretValue);
        var secretsReader = new SecretsReader(fakeSecretsManagerClient);
        assertThat(secretsReader.fetchSecret(secretName, secretKey), is(equalTo(secretValue)));
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
