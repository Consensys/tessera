package com.github.nexus.api.filter;

import com.github.nexus.configuration.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

/**
 * Applies a filter to all endpoints that only allows certain IP address and ghost names to
 * get access to the HTTP endpoints
 *
 * If an error occurs whilst checking the whitelist,
 * the filter is disabled. This is done since not all webservers have support for
 * the {@link HttpServletRequest} context class, which is required.
 */
@GlobalFilter
public class IPWhitelistFilter implements ContainerRequestFilter {

    private final Set<String> whitelisted;

    private boolean disabled;

    private HttpServletRequest httpServletRequest;

    public IPWhitelistFilter(final Configuration configuration) {
        this.whitelisted = new HashSet<>(configuration.whitelist());
        this.disabled = this.whitelisted.isEmpty();
    }

    /**
     * If the filter is disabled, return immediately
     * Otherwise, extract the callers hostname and address, and check it against the whitelist
     *
     * If a problem occurs, then disable the filter
     *
     * If the host is not whitelisted, finish the filter chain here and return an Unauthorized response
     *
     * @param requestContext the context of the current request
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {

        if(disabled) {
            return;
        }

        try {

            final String remoteAddress = httpServletRequest.getRemoteAddr();
            final String remoteHost = httpServletRequest.getRemoteHost();

            final boolean allowed = whitelisted.contains(remoteAddress) || whitelisted.contains(remoteHost);

            if (!allowed) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }

        } catch (final Exception ex) {
            this.disabled = true;
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
