package com.quorum.tessera.api.filter;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.ssl.util.HostnameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

@PrivateApi
public class PrivateApiFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateApiFilter.class);

    private HttpServletRequest httpServletRequest;

    private final String hostname;

    public PrivateApiFilter(final ServerConfig serverConfig) {
        try {
            this.hostname = new URI(serverConfig.getBindingAddress()).getHost();
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
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
    public void filter(final ContainerRequestContext requestContext) throws UnknownHostException {

        if(this.httpServletRequest == null) {
            LOGGER.debug("No servlet available, could not determine request origin. Allowing...");
            return;
        }

        final String remoteAddress = httpServletRequest.getRemoteAddr();
        final String remoteHost = httpServletRequest.getRemoteHost();

//        LOGGER.info("Allowed host: {}, RemoteAddr: {}, RemoteHost: {}", hostname, remoteAddress, remoteHost);
        LOGGER.info("Allowed host: {}", hostname);
        LOGGER.info("This host: {}", HostnameUtil.create().getHostName());
        LOGGER.info("This IP: {}", HostnameUtil.create().getHostIpAddress());
        LOGGER.info("Remote host : {}", remoteAddress);
        LOGGER.info("Remote host : {}", remoteHost);
        LOGGER.info("Request uri : {}", httpServletRequest.getRequestURI());




        final boolean allowed = hostname.equals(remoteAddress) || hostname.equals(remoteHost);

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
