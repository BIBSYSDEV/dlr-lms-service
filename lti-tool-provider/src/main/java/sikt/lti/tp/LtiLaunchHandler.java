package sikt.lti.tp;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UriWrapper;
import sikt.lti.tp.cc.CommonCartridgeGenerator;
import sikt.lti.tp.cc.TemplateBasedCommonCartridgeGenerator;

public class LtiLaunchHandler {

    private static final String APPLICATION_XML = "application/xml; charset=utf-8";
    private static final String TEXT_PLAIN = "text/plain; charset=utf-8";
    private static final String TEXT_HTML = "text/html; charset=utf-8";
    private static final String SUPPORTED_LTI_MESSAGE_TYPE = "basic-lti-launch-request";
    private static final String SUPPORTED_LTI_VERSION = "LTI-1p0";

    private final URI apiBaseUrl;
    private final URI dlrBaseUrl;
    private final String serviceId;
    private final Map<String, String> parameters;
    private final Set<String> knownConsumerKeys;
    private final CommonCartridgeGenerator commonCartridgeGenerator = new TemplateBasedCommonCartridgeGenerator();

    protected LtiLaunchHandler(final URI apiBaseUrl,
                               final URI dlrBaseUrl,
                               final String serviceId,
                               final Map<String, String> parameters,
                               final Set<String> knownConsumerKeys) {
        this.apiBaseUrl = apiBaseUrl;
        this.dlrBaseUrl = dlrBaseUrl;
        this.serviceId = serviceId;
        this.parameters = parameters;
        this.knownConsumerKeys = knownConsumerKeys;
    }

    public LtiLaunchResult execute() {
        final Optional<ServiceIdentifier> serviceIdentifier = getServiceIdentifier(String.valueOf(serviceId));

        if (serviceIdentifier.isEmpty()) {
            return new LtiLaunchResult(200, TEXT_HTML, generateListOfServicesAsHtml());
        }

        LtiLaunchResult result;
        if (parameters.isEmpty()) {
            final String body = commonCartridgeGenerator.generate(serviceIdentifier.get(), apiBaseUrl);
            result = new LtiLaunchResult(200, APPLICATION_XML, body);
        } else {
            result = handleLtiLaunchRequest(serviceIdentifier.get(), parameters);
        }
        return result;
    }

    private LtiLaunchResult handleLtiLaunchRequest(final ServiceIdentifier serviceIdentifier,
                                                   final Map<String, String> parameters) {
        final String oauthConsumerKey = parameters.get(LtiLaunchParameter.OAUTH_CONSUMER_KEY.getValue());
        final String ltiMessageType = parameters.get(LtiLaunchParameter.LTI_MESSAGE_TYPE.getValue());
        final String ltiVersion = parameters.get(LtiLaunchParameter.LTI_VERSION.getValue());
        final String resourceLinkId = parameters.get(LtiLaunchParameter.RESOURCE_LINK_ID.getValue());
        @SuppressWarnings("PMD.PrematureDeclaration") // todo: for now
        final String returnUrl = parameters.get(LtiLaunchParameter.LAUNCH_PRESENTATION_RETURN_URL.getValue());

        final LtiLaunchResult constraintCheckResult = checkLaunchParameterConstraints(oauthConsumerKey,
                                                                                      ltiMessageType,
                                                                                      ltiVersion,
                                                                                      resourceLinkId);
        if (constraintCheckResult != null) {
            return constraintCheckResult;
        }

        final LtiLaunchResult result;
        switch (serviceIdentifier) {
            case site:
                result = handleSiteLaunch();
                break;
            case embedExternalTool:
                result = handleEmbedExternalToolLaunch(returnUrl);
                break;
            case embedRichContentEditor:
                result = handleEmbedRichContentEditorLaunch(returnUrl);
                break;
            case combined:
            default:
                result = new LtiLaunchResult(200, TEXT_HTML, generateListOfServicesAsHtml());
                break;
        }
        return result;
    }

    private LtiLaunchResult checkLaunchParameterConstraints(final String oauthConsumerKey,
                                                            final String ltiMessageType,
                                                            final String ltiVersion,
                                                            final String resourceLinkId) {
        if (StringUtils.isEmpty(oauthConsumerKey)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Missing required param 'oauth_consumer_key'");
        } else if (StringUtils.isEmpty(ltiMessageType)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Missing required param 'lti_message_type'");
        } else if (StringUtils.isEmpty(ltiVersion)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Missing required param 'lti_version'");
        } else if (StringUtils.isEmpty(resourceLinkId)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Missing required param 'resource_link_id'");
        } else if (!SUPPORTED_LTI_MESSAGE_TYPE.equals(ltiMessageType)) {
            final String body = String.format("Only supports value '%s' for param '%s'",
                                              SUPPORTED_LTI_MESSAGE_TYPE,
                                              LtiLaunchParameter.LTI_MESSAGE_TYPE.getValue());
            return new LtiLaunchResult(401, TEXT_PLAIN, body);
        } else if (!SUPPORTED_LTI_VERSION.equals(ltiVersion)) {
            final String body = String.format("Only supports value '%s' for param '%s'", SUPPORTED_LTI_VERSION,
                                              LtiLaunchParameter.LTI_VERSION.getValue());
            return new LtiLaunchResult(401, TEXT_PLAIN, body);
        } else if (!knownConsumerKeys.contains(oauthConsumerKey)) {
            return new LtiLaunchResult(401, TEXT_PLAIN, "Unknown consumer");
        } else {
            return null;
        }
    }

    private LtiLaunchResult handleEmbedExternalToolLaunch(final String returnUrl) {
        final String location = UriWrapper.fromUri(dlrBaseUrl)
                                    .addQueryParameter("forceAuthentication", Boolean.toString(true))
                                    .addQueryParameter("canvasShowEmbedLinkButton", Boolean.toString(true))
                                    .addQueryParameter("canvasIframeResize", Boolean.toString(true))
                                    .addQueryParameter("canvasLaunchPresentationReturnUrl", returnUrl)
                                    .toString();
        return new LtiLaunchResult(location);
    }

    private LtiLaunchResult handleEmbedRichContentEditorLaunch(final String returnUrl) {
        final String location = UriWrapper.fromUri(dlrBaseUrl)
                                    .addQueryParameter("forceAuthentication", Boolean.toString(true))
                                    .addQueryParameter("canvasShowEmbedButton", Boolean.toString(true))
                                    .addQueryParameter("canvasIframeResize", Boolean.toString(true))
                                    .addQueryParameter("canvasLaunchPresentationReturnUrl", returnUrl)
                                    .toString();
        return new LtiLaunchResult(location);
    }

    private LtiLaunchResult handleSiteLaunch() {
        final String location = UriWrapper.fromUri(dlrBaseUrl)
                                    .addQueryParameter("forceAuthentication", Boolean.toString(true))
                                    .addQueryParameter("canvasIframeResize", Boolean.toString(true))
                                    .toString();
        return new LtiLaunchResult(location);
    }

    private String generateListOfServicesAsHtml() {
        final StringBuilder builder = new StringBuilder(256);

        builder.append("<html>\n<body>\nAvailable LTI services:<br/><br/>\n");

        for (ServiceIdentifier identifier : ServiceIdentifier.values()) {
            final UriWrapper uriWrapper = UriWrapper.fromUri(apiBaseUrl)
                                              .addChild("lms", "lti", "canvas", identifier.name());
            final String url = uriWrapper.toString();
            builder.append("<a href=\"")
                .append(uriWrapper)
                .append("\">")
                .append(url)
                .append("</a><br/>\n");
        }

        return builder.append("</body>\n</html>").toString();
    }

    private Optional<ServiceIdentifier> getServiceIdentifier(final String serviceId) {
        try {
            return Optional.of(ServiceIdentifier.valueOf(serviceId));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
