package com.quorum.tessera.thirdparty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Extension
public class PluginResourceImpl implements PluginResource{
  @GET
  @Path("/plugins")
  @Produces(MediaType.TEXT_PLAIN)
  public Response get() {
    return Response.ok().build();
  }
}
