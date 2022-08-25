package sikt.lti.tp;

import java.net.URI;

public class Consumer {
    private final String consumerKey;
    private final String consumerSecret;
    private final URI consumerUri;

    public Consumer(String consumerKey, String consumerSecret, URI consumerUri) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.consumerUri = consumerUri;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public URI getConsumerUri() {
        return consumerUri;
    }
}
