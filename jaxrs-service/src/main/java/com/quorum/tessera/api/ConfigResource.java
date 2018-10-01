package com.quorum.tessera.api;

import com.quorum.tessera.api.filter.PrivateApi;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
        
        int index = configService.getPeers().size() - 1;

        URI uri = UriBuilder.fromPath("config")
                .path("peers")
                .path(String.valueOf(index))
                .build();
        return Response.created(uri).build();
    }

    @GET
    @Path("/peers/{index}")
    public Response getPeer(@PathParam("index") Integer index) {

        List<Peer> peers = configService.getPeers();
        if (peers.size() <= index) {
            throw new NotFoundException("No peer found at index "+ index);
        }
        return Response.ok(peers.get(index)).build();
    }

}
