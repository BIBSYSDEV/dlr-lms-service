package sikt.lti.tp;

public class LtiLaunchResult {

    private final int status;
    private final String contentType;
    private final String body;
    private final String location;

    public LtiLaunchResult(final int status, final String contentType, final String body) {
        this.status = status;
        this.contentType = contentType;
        this.body = body;
        this.location = "";
    }

    public LtiLaunchResult(final String location) {
        this.status = 304;
        this.contentType = "";
        this.body = "";
        this.location = location;
    }

    public int getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }

    public String getLocation() {
        return location;
    }
}
