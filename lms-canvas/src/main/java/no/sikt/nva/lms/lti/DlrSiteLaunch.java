package no.sikt.nva.lms.lti;

import java.util.spi.ToolProvider;
import javax.security.auth.callback.Callback;
import org.oscelot.lti.tp.Callback;
import org.oscelot.lti.tp.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DlrSiteLaunch implements Callback {

    private static final Logger logger = LoggerFactory.getLogger(DlrSiteLaunch.class);


    /*
     * Only if the request passes all checks (OAuth, parameter validation etc) is the callback class executed
     *
     * The callback function may be used to:
     *
     * - create the user account if it does not already exist (or update it if it does);
     * - create the resource link area if it does not already exist (or update it if it does);
     * - set up a new session for the user (or otherwise log the user into the tool provider application);
     * - keep a record of the return URL for the tool consumer (for example, as a session variable);
     * - set the URL for the home page of the application so the user may be redirected to it;
     * - return false if the connection request is not to be accepted (optionally setting an error message to be returned).
     *
     *
     * @param toolProvider
     * @return
     */
    @Override
    public boolean execute(ToolProvider toolProvider) {

        HttpServletRequest request = toolProvider.getRequest();

        HttpSession session = request.getSession(true);

        session.setAttribute("TOOL_PROVIDER", toolProvider);

        String dlr_host = System.getProperty("dlr_host");
        logger.info("DlrSiteLaunch hostname: " + dlr_host);

        toolProvider.setRedirectUrl(dlr_host + "/?forceAuthentication=true&canvasIframeResize=true");

        return true;
    }
}

