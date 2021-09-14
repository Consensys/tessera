package com.quorum.tessera.server.jaxrs;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

  @Context private ResourceInfo resourceInfo;

  private Logger getLogger() {
    return Optional.ofNullable(resourceInfo)
        .filter(r -> r.getResourceClass() != null)
        .map(r -> LoggerFactory.getLogger(r.getResourceClass()))
        .orElse(LOGGER);
  }

  @Override
  public void filter(final ContainerRequestContext request) {
    log("Enter", request);
  }

  @Override
  public void filter(
      final ContainerRequestContext request, final ContainerResponseContext response) {
    log("Exit", request);
    String path = Optional.ofNullable(request.getUriInfo()).map(UriInfo::getPath).orElse(null);
    Optional.ofNullable(response.getStatusInfo())
        .ifPresent(
            statusType ->
                getLogger()
                    .info(
                        "Response for {} : {} {}",
                        path,
                        statusType.getStatusCode(),
                        statusType.getReasonPhrase()));
  }

  private void log(String prefix, ContainerRequestContext request) {
    String path = Optional.ofNullable(request.getUriInfo()).map(UriInfo::getPath).orElse(null);
    getLogger().info("{} Request : {} : {}", prefix, request.getMethod(), "/" + path);
  }

  /**
   * Set the request resource info. Only needed for unit tests.
   *
   * @param resourceInfo the resource info
   */
  @Context
  public void setResourceInfo(final ResourceInfo resourceInfo) {
    this.resourceInfo = resourceInfo;
  }
}
