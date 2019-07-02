package com.quorum.tessera.server.jaxrs;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class SampleResource {

    private Map<String, SamplePayload> store = new HashMap<>();

    @Path("ping")
    @GET
    public String ping() {
        System.out.println("PING");
        return "HEllow";
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
    public Response create(SamplePayload payload, @Context UriInfo uriInfo) throws UnsupportedEncodingException {
        System.out.println("CREATE" + payload);
        String id = UUID.randomUUID().toString();
        payload.setId(id);
        store.put(id, payload);

        URI location = uriInfo.getBaseUriBuilder().path("find").path(URLEncoder.encode(id, "UTF-8")).build();
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
