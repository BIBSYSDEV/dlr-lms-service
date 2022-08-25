package sikt.lti.tp;

import java.net.URI;
import java.util.Map;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;
import sikt.lti.tp.cc.CommonCartridgeGenerator;
import sikt.lti.tp.cc.TemplateBasedCommonCartridgeGenerator;

public abstract class LtiLaunchHandler {

    private static final String APPLICATION_XML = "application/xml; charset=utf-8";
    private static final String TEXT_PLAIN = "text/plain; charset=utf-8";
    private static final String TEXT_HTML = "text/html; charset=utf-8";

    private final URI apiBaseUrl;
    private final URI dlrBaseUrl;
    private final String path;
    private final Map<String, String> parameters;
    private final Map<String, Consumer> consumerByKeyMap;
    private final CommonCartridgeGenerator commonCartridgeGenerator = new TemplateBasedCommonCartridgeGenerator();

    protected LtiLaunchHandler(final URI apiBaseUrl,
                               URI dlrBaseUrl, final String path,
                               final Map<String, String> parameters,
                               final Map<String, Consumer> consumerByKeyMap) {
        this.apiBaseUrl = apiBaseUrl;
        this.dlrBaseUrl = dlrBaseUrl;
        this.path = path;
        this.parameters = parameters;
        this.consumerByKeyMap = consumerByKeyMap;
    }

    public LtiLaunchResult execute() {
        final ServiceIdentifier serviceIdentifier = getServiceIdentifier(path);
        if (serviceIdentifier == null) {
            return new LtiLaunchResult(200, TEXT_HTML, generateListOfServicesAsHtml());
        }

        LtiLaunchResult result;
        if (parameters.isEmpty()) {
            final String body = commonCartridgeGenerator.generate(serviceIdentifier, apiBaseUrl);
            result =  new LtiLaunchResult(200, APPLICATION_XML, body);
        } else {
            result = handleLtiLaunchRequest(serviceIdentifier, parameters);
        }
        return result;
    }

    private LtiLaunchResult handleLtiLaunchRequest(final ServiceIdentifier serviceIdentifier,
                                                   final Map<String, String> parameters) {
        final String oauthConsumerKey = parameters.get(LtiLaunchParameter.OAUTH_CONSUMER_KEY.getValue());
        if (StringUtils.isEmpty(oauthConsumerKey)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Missing required param &#39;oauth_consumer_key&#39;");
        }
        final String ltiMessageType = parameters.get(LtiLaunchParameter.LTI_MESSAGE_TYPE.getValue());
        if (StringUtils.isEmpty(ltiMessageType)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Missing required param &#39;lti_message_type&#39;");
        }
        final String ltiVersion = parameters.get(LtiLaunchParameter.LTI_VERSION.getValue());
        if (StringUtils.isEmpty(ltiVersion)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Missing required param &#39;lti_version&#39;");
        }
        final String resourceLinkId = parameters.get(LtiLaunchParameter.RESOURCE_LINK_ID.getValue());
        if (StringUtils.isEmpty(resourceLinkId)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Missing required param &#39;resource_link_id&#39;");
        }
        final String returnUrl = parameters.get(LtiLaunchParameter.LAUNCH_PRESENTATION_RETURN_URL.getValue());

        final LtiLaunchResult result;
        switch (serviceIdentifier) {
            case site:
                result = handleSiteLaunch();
                break;
            case embedExternalTool:
                result = handleEmbedExternalToolLaunch(returnUrl);
                break;
            case embedRichContentEditor:
                result = handleEmbedRichContentEditorLaunch();
                break;
            case combined:
            default:
                result = new LtiLaunchResult(500, "", "");
                break;
        }
        return result;
    }

    private LtiLaunchResult handleEmbedExternalToolLaunch(final String returnUrl) {
        final String redirectLocation = UriWrapper.fromUri(dlrBaseUrl)
            .addQueryParameter("forceAuthentication", "true")
            .addQueryParameter("canvasShowEmbedLinkButton", "true")
            .addQueryParameter("canvasIframeResize", "true")
            .addQueryParameter("canvasLaunchPresentationReturnUrl", returnUrl)
            .toString();
        return new LtiLaunchResult(304, "", redirectLocation);
    }

    private LtiLaunchResult handleEmbedRichContentEditorLaunch() {
        return null;
    }

    private LtiLaunchResult handleSiteLaunch() {
        return null;
    }

    private String generateListOfServicesAsHtml() {
        final StringBuilder builder = new StringBuilder();

        builder.append("<html>\n");
        builder.append("<body>\n");

        builder.append("Available LTI services:<br/><br/>\n");

        for (ServiceIdentifier identifier : ServiceIdentifier.values()) {
            final UriWrapper uriWrapper = UriWrapper.fromUri(apiBaseUrl)
                                              .addChild("lms", "canvas", "v1", identifier.name());
            final String url = uriWrapper.toString();
            builder.append("<a href=\"")
                .append(uriWrapper.toString())
                .append("\">")
                .append(url)
                .append("</a><br/>\n");
        }

        builder.append("</body>\n");
        builder.append("</html>");

        return builder.toString();
    }

    private ServiceIdentifier getServiceIdentifier(final String path) {
        final UnixPath unixPath = UnixPath.fromString(path);

        try {
            return ServiceIdentifier.valueOf(unixPath.getLastPathElement());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
