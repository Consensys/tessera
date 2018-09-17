package com.quorum.tessera.api.filter;

import com.quorum.tessera.ssl.util.HostnameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;

@PrivateApi
public class PrivateApiFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateApiFilter.class);

    private static final String LOCALHOST = "127.0.0.1";

    private HttpServletRequest httpServletRequest;

    private final String hostname;

    public PrivateApiFilter() throws UnknownHostException {
        this.hostname = HostnameUtil.create().getHostIpAddress();
    }

    /**
     * Extract the callers hostname and address,
     * and check it against our configured hostname to check they are the same
     * <p>
     * If the host is not the same as ours, finish the filter chain here and return an Unauthorized response
     *
     * @param requestContext the context of the current request
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {

        if(this.httpServletRequest == null) {
            LOGGER.debug("No servlet available, could not determine request origin. Allowing...");
            return;
        }

        final String remoteAddress = httpServletRequest.getRemoteAddr();

        final boolean allowed = hostname.equals(remoteAddress) || LOCALHOST.equals(remoteAddress);

        if (!allowed) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }

    }

    /**
     * Apply the current HTTP context to the filter, to check the remote host
     *
     * @param request the request to be filtered
     */
    @Context
    public void setHttpServletRequest(final HttpServletRequest request) {
        this.httpServletRequest = request;
    }
}
