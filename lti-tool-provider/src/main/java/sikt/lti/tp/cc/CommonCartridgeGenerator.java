package sikt.lti.tp.cc;

import java.net.URI;
import sikt.lti.tp.ServiceIdentifier;

public interface CommonCartridgeGenerator {
    String generate(ServiceIdentifier serviceIdentifier, URI apiBaseUrl);
}
