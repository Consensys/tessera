package com.quorum.tessera.server.http;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class SomeResource {

  @Path("ping")
  @GET
  public String ping() {
    System.out.println("PING");
    return "ping";
  }
}
