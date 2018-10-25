package com.quorum.tessera.api;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

    private final ConfigService configService;

    public ConfigResource(final ConfigService configService) {
        this.configService = Objects.requireNonNull(configService);
    }

    @PUT
    @Path("/peers")
    public Response addPeer(@Valid final Peer peer) {

        this.configService.addPeer(peer.getUrl());

        //TODO: this seems a bit presumptuous, search for the peer instead?
        final int index = this.configService.getPeers().size() - 1;

        final URI uri = UriBuilder.fromPath("config")
                .path("peers")
                .path(String.valueOf(index))
                .build();

        return Response.created(uri).build();
    }

    @GET
    @Path("/peers/{index}")
    public Response getPeer(@PathParam("index") final Integer index) {

        final List<Peer> peers = this.configService.getPeers();

        if (peers.size() <= index) {
            throw new NotFoundException("No peer found at index "+ index);
        }

        return Response.ok(peers.get(index)).build();
    }

}
