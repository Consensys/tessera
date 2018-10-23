package com.quorum.tessera.api.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

/**
 * The filter logic that only allows requests to certain hosts to hit HTTP endpoints
 */
@PrivateApi
public class PrivateApiFilter implements ContainerRequestFilter {

    //Allow the Unix Socket host to receive the requests to private endpoints
    private static final String UNIX_SOCKET = "unixsocket";

    //TODO: this should be changed to be injected
    //This is used to allow Private API endpoints to be called over the HTTP server,
    //instead of via the Unix Domain Socket
    private final boolean isDebug = Boolean.valueOf(System.getProperty("debug", "false"));

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

        final String baseUri = requestContext.getUriInfo().getBaseUri().toString();

        final boolean allowed = isDebug || UNIX_SOCKET.equals(baseUri);

        if (!allowed) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }

    }

}
