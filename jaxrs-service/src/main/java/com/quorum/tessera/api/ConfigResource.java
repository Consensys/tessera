package com.quorum.tessera.api;

import com.quorum.tessera.api.filter.PrivateApi;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;
import java.net.URI;
import java.util.Objects;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@PrivateApi
@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

    private final ConfigService configService;

    public ConfigResource(ConfigService configService) {
        this.configService = Objects.requireNonNull(configService);
    }

    @PUT
    @Path("/peers")
    public Response addPeer(@Valid Peer peer) {
        configService.addPeer(peer.getUrl());
        URI uri = UriBuilder.fromMethod(ConfigResource.class, "getPeer").build(peer.getUrl());
        return Response.created(uri).build();
    }

    
    @GET
    @Path("/peers/{url}")
    public Response getPeer(@PathParam("url") String url) {

        return configService.getPeers().stream()
                .filter(p -> Objects.equals(p.getUrl(), url))
                .map(p -> Response.ok(p))
                .map(r -> r.build())
                .findAny()
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }   

}
