package com.quorum.tessera.api.filter;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.io.IOCallback;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies a filter to all endpoints that only allows certain IP address and ghost names to get access to the HTTP
 * endpoints
 *
 * <p>If an error occurs whilst checking the whitelist, the filter is disabled. This is done since not all webservers
 * have support for the {@link HttpServletRequest} context class, which is required.
 */
@GlobalFilter
public class IPWhitelistFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPWhitelistFilter.class);

    private final ConfigService configService;

    private boolean disabled;

    private HttpServletRequest httpServletRequest;

    public IPWhitelistFilter(ConfigService configService) {
        this.configService = Objects.requireNonNull(configService);
        this.disabled = !configService.isUseWhiteList();
    }

    /**
     * If the filter is disabled, return immediately Otherwise, extract the callers hostname and address, and check it
     * against the whitelist
     *
     * <p>If a problem occurs, then disable the filter
     *
     * <p>If the host is not whitelisted, finish the filter chain here and return an Unauthorized response
     *
     * @param requestContext the context of the current request
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {

        if (disabled) {
            return;
        }

        // this is the unix socket request, so let it through the filter
        if ("unixsocket".equals(requestContext.getUriInfo().getBaseUri().toString())) {
            return;
        }

        try {

            final Set<String> whitelisted =
                    configService.getPeers().stream()
                            .map(Peer::getUrl)
                            .map(s -> IOCallback.execute(() -> new URL(s)))
                            .map(URL::getHost)
                            .collect(Collectors.toSet());

            final String remoteAddress = httpServletRequest.getRemoteAddr();
            final String remoteHost = httpServletRequest.getRemoteHost();

            final boolean allowed = whitelisted.contains(remoteAddress) || whitelisted.contains(remoteHost);

            if (!allowed) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }

        } catch (final Exception ex) {
            LOGGER.error("Unexpected error while processing request.", ex);
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
