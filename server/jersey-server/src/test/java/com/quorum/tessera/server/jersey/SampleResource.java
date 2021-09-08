package com.quorum.tessera.server.jersey;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Singleton
@Path("/")
public class SampleResource {

  private final Ping ping;

  @Inject
  public SampleResource(@Named("myBean") Ping ping) {
    this.ping = Objects.requireNonNull(ping);
  }

  private Map<String, SamplePayload> store = new HashMap<>();

  @Path("ping")
  @GET
  public String ping() {
    System.out.println("PING");
    return ping.ping();
  }

  @Produces(MediaType.APPLICATION_JSON)
  @GET
  @Path("find/{id}")
  public Response find(@PathParam("id") String id) {
    System.out.println("FIND " + id);
    SamplePayload payload = store.get(id);
    return Response.ok(payload, MediaType.APPLICATION_JSON).build();
  }

  @Consumes(MediaType.APPLICATION_JSON)
  @Path("create")
  @POST
  public Response create(SamplePayload payload, @Context UriInfo uriInfo)
      throws UnsupportedEncodingException {
    System.out.println("CREATE" + payload);
    String id = UUID.randomUUID().toString();
    payload.setId(id);
    store.put(id, payload);

    URI location =
        uriInfo.getBaseUriBuilder().path("find").path(URLEncoder.encode(id, "UTF-8")).build();
    System.out.println("CREATE " + location);
    return Response.status(Response.Status.CREATED).location(location).build();
  }

  @Path("{id}")
  @DELETE
  public Response delete(@PathParam("id") String id) {
    SamplePayload deleted = store.remove(id);

    return Response.ok(deleted).build();
  }
}
