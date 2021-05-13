package com.quorum.tessera.server.http;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class SomeResource {

  @Path("ping")
  @GET
  public String ping() {
    System.out.println("PING");
    return "ping";
  }
}
