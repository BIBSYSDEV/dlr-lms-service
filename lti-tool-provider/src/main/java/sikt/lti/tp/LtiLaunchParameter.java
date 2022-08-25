package sikt.lti.tp;

public enum LtiLaunchParameter {
    OAUTH_CONSUMER_KEY("oauth_consumer_key"),
    LTI_MESSAGE_TYPE("lti_message_type"),
    LTI_VERSION("lti_version"),
    RESOURCE_LINK_ID("resource_link_id"),
    LAUNCH_PRESENTATION_RETURN_URL("launch_presentation_return_url");

    private final String value;

    LtiLaunchParameter(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
