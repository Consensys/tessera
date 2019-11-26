package com.quorum.tessera.admin;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.encryption.PublicKey;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import io.swagger.annotations.Api;
@Api
@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

    private final ConfigService configService;

    private final PartyInfoService partyInfoService;

    public ConfigResource() {

        ServiceFactory serviceFactory = ServiceFactory.create();

        this.configService = serviceFactory.configService();

        this.partyInfoService = serviceFactory.partyInfoService();
    }

    public ConfigResource(final ConfigService configService, final PartyInfoService partyInfoService) {
        this.configService = Objects.requireNonNull(configService);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @PUT
    @Path("/peers")
    public Response addPeer(@Valid final Peer peer) {

        final boolean existing = configService.getPeers().contains(peer);

        if (!existing) {
            this.configService.addPeer(peer.getUrl());

            this.partyInfoService.updatePartyInfo(
                    new PartyInfo(peer.getUrl(), emptySet(), singleton(new Party(peer.getUrl()))));
        }

        final int index = this.configService.getPeers().indexOf(peer);

        final URI uri = UriBuilder.fromPath("config").path("peers").path(String.valueOf(index)).build();

        if (!existing) {
            return Response.created(uri).build();
        } else {
            return Response.ok().location(uri).build();
        }
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

        return Response.ok(new GenericEntity<List<Peer>>(peers) {}).build();
    }

    @GET
    @Path("/keypairs/{publicKey}")
    public Response getKeyPair(@PathParam("publicKey") String base64PublicKey) {

        Base64.Decoder base64Decoder = Base64.getDecoder();

        PublicKey publicKey = PublicKey.from(base64Decoder.decode(base64PublicKey));

        Set<PublicKey> publicKeys = configService.getPublicKeys();

        if (!publicKeys.contains(publicKey)) {
            throw new NotFoundException("No key pair found with public key " + base64PublicKey);
        }

        PublicKeyResponse responseData = new PublicKeyResponse(base64PublicKey);

        return Response.ok(responseData).build();
    }

    @GET
    @Path("/keypairs")
    public Response getKeyPairs() {
        Set<PublicKey> publicKeys = configService.getPublicKeys();

        List<PublicKeyResponse> responseData =
                publicKeys.stream()
                        .map(PublicKey::encodeToBase64)
                        .map(PublicKeyResponse::new)
                        .collect(Collectors.toList());

        return Response.ok(new GenericEntity<List<PublicKeyResponse>>(responseData) {}).build();
    }
}
