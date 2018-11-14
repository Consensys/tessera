package com.quorum.tessera.api;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.GenericEntity;

@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

    private final ConfigService configService;

    private final PartyInfoService partyInfoService;

    public ConfigResource(ConfigService configService, 
            PartyInfoService partyInfoService) {
        this.configService = Objects.requireNonNull(configService);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }
    


    @PUT
    @Path("/peers")
    public Response addPeer(@Valid final Peer peer) {

        this.configService.addPeer(peer.getUrl());

        partyInfoService.updatePartyInfo(
                new PartyInfo(peer.getUrl(),Collections.EMPTY_SET, 
                        Collections.singleton(new Party(peer.getUrl()))));
        
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
            throw new NotFoundException("No peer found at index " + index);
        }

        return Response.ok(peers.get(index)).build();
    }

    @GET
    @Path("/peers")
    public Response getPeers() {
        final List<Peer> peers = this.configService.getPeers();

        return Response.ok(new GenericEntity<List<Peer>>(peers) {
        }).build();
    }

}
