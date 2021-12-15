package com.quorum.tessera.api.common;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns a 200 for GET requests made to http://server and http://server/
 *
 * <p>This is required by Kubernetes Ingress load balancers which require '/' return 200 for their
 * health-checks. https://github.com/jpmorganchase/tessera/issues/1064
 */
@io.swagger.v3.oas.annotations.Hidden
@Path("/")
public class BaseResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseResource.class);

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response get() {
    LOGGER.debug("GET /");
    return Response.ok().build();
  }
}
