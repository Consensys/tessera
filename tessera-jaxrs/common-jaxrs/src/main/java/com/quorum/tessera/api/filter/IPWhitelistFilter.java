package com.quorum.tessera.api.filter;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.io.IOCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * If peer discovery is enabled, a filter to all endpoints that only allows certain IP address
 * and host names to get access to the HTTP endpoints is applied.
 *
 * @see Config#isDisablePeerDiscovery()
 */
@GlobalFilter
public class IPWhitelistFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPWhitelistFilter.class);

    private final ConfigService configService;

    private final boolean enablePeerDiscovery;

    private HttpServletRequest httpServletRequest;

    public IPWhitelistFilter() {
        this(ServiceFactory.create().configService());
    }

    protected IPWhitelistFilter(ConfigService configService) {
        this.configService = configService;
        this.enablePeerDiscovery = !configService.isDisablePeerDiscovery();
    }

    /**
     * If the filter is disabled, return immediately Otherwise, extract the callers hostname and address, and check it
     * against the whitelist
     * <p>If the host is not whitelisted, finish the filter chain here and return an Unauthorized response
     *
     * @param requestContext the context of the current request
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {

        if (enablePeerDiscovery) {
            LOGGER.debug("Peer discovery enabled. no whitelist filtering required.");
            return;
        }

        if ("unixsocket".equals(Objects.toString(requestContext.getUriInfo().getBaseUri()))) {
            LOGGER.trace("Ignore requests over unixsocket.");
            return;
        }

        final Set<String> whitelisted =
            configService.getPeers().stream()
                .map(Peer::getUrl)
                .map(s -> IOCallback.execute(() -> new URL(s)))
                .map(URL::getHost)
                .collect(Collectors.toSet());

        final String remoteAddress = httpServletRequest.getRemoteAddr();
        final String remoteHost = httpServletRequest.getRemoteHost();

        LOGGER.debug("Check if whitelist {} contains {} or {}",whitelisted,remoteAddress,remoteHost);

        final boolean allowed = whitelisted.contains(remoteAddress) || whitelisted.contains(remoteHost);

        if (allowed) {
            LOGGER.trace("Found hostname {} or address in whitelist", remoteAddress,remoteHost);
            return;
        }

        LOGGER.warn("Unauthorised request from remote address {} : remote host : {}",remoteAddress,remoteHost);
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());

    }

    @Context
    public void setHttpServletRequest(final HttpServletRequest request) {
        this.httpServletRequest = request;
    }
}
