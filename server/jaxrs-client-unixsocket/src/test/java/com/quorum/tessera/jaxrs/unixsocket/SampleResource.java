package com.quorum.tessera.jaxrs.unixsocket;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

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

    @POST
    @Path("sendraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    public Response sendRaw(
            @HeaderParam("c11n-from") final String sender,
            @HeaderParam("c11n-to") final String recipientKeys,
            final byte[] payload,
            @Context UriInfo uriInfo)
            throws UnsupportedEncodingException {

        String id = UUID.randomUUID().toString();
        URI location = uriInfo.getBaseUriBuilder().path("raw").path(URLEncoder.encode(id, "UTF-8")).build();

        return Response.created(location).build();
    }

    @Path("param")
    @GET
    public Response withparam(@HeaderParam("headerParam") String headerParam, @QueryParam("queryParam") String qparam) {

        System.out.println("headerParam: " + headerParam);
        System.out.println("QueryParam: " + qparam);

        return Response.ok().build();
    }
}
