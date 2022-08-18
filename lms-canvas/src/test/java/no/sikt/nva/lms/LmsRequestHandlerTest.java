package no.sikt.nva.lms;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LmsRequestHandlerTest {

    public static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    private final Context context = new FakeContext();
    private LmsRequestHandler handler;
    private Environment environment;

    @BeforeEach
    public void init() {
        this.environment = new Environment();

        handler = new LmsRequestHandler(Void.class, environment);
    }

    @Test
    void returnSomething() throws IOException {
        handler.handleRequest(new HandlerRequestBuilder<Void>(OBJECT_MAPPER).build(), new ByteArrayOutputStream(),
                              context);
    }
}
