package com.quorum.tessera.api.filter;

import com.quorum.tessera.context.RuntimeContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies a filter to all endpoints that only allows certain IP address and ghost names to get
 * access to the HTTP endpoints
 *
 * <p>If an error occurs whilst checking the whitelist, the filter is disabled. This is done since
 * not all webservers have support for the {@link HttpServletRequest} context class, which is
 * required.
 */
@GlobalFilter
public class IPWhitelistFilter implements ContainerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(IPWhitelistFilter.class);

  private HttpServletRequest httpServletRequest;

  /**
   * If the filter is disabled, return immediately Otherwise, extract the callers hostname and
   * address, and check it against the whitelist
   *
   * <p>If a problem occurs, then disable the filter
   *
   * <p>If the host is not whitelisted, finish the filter chain here and return an Unauthorized
   * response
   *
   * @param requestContext the context of the current request
   */
  @Override
  public void filter(final ContainerRequestContext requestContext) {
    RuntimeContext runtimeContext = RuntimeContext.getInstance();
    if (!runtimeContext.isUseWhiteList()) {
      return;
    }

    final Set<String> whitelisted =
        runtimeContext.getPeers().stream().map(URI::getHost).collect(Collectors.toSet());

    // If local host is whitelisted then ensure all the various forms are allowed, including the
    // IPv6 localhost
    // as sent by curl
    if (whitelisted.contains("localhost") || whitelisted.contains("127.0.0.1")) {
      whitelisted.add("localhost");
      whitelisted.add("127.0.0.1");
      whitelisted.add("0:0:0:0:0:0:0:1");
    }

    final String remoteAddress = httpServletRequest.getRemoteAddr();
    final String remoteHost = httpServletRequest.getRemoteHost();

    final boolean allowed =
        whitelisted.stream().anyMatch(v -> Arrays.asList(remoteAddress, remoteHost).contains(v));

    if (!allowed) {
      LOGGER.warn(
          "Remote host {} with IP {} failed whitelist validation", remoteHost, remoteAddress);
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
