package sikt.lti.tp.cc;

import java.net.URI;
import java.nio.file.Path;
import nva.commons.core.ioutils.IoUtils;
import sikt.lti.tp.ServiceIdentifier;

public class TemplateBasedCommonCartridgeGenerator implements CommonCartridgeGenerator {
    private static final String API_HOST_PLACEHOLDER = "@@API_HOST@@";

    @Override
    public String generate(final ServiceIdentifier serviceIdentifier, final URI apiBaseUrl) {
        final Path path = Path.of("cartridge/combinedCanvasCommonCartridgeTemplate.xml");
        final String template = IoUtils.stringFromResources(path);

        return template.replaceAll(API_HOST_PLACEHOLDER, apiBaseUrl.toString());
    }
}
