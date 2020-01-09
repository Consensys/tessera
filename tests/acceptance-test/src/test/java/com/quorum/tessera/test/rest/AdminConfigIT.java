package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.test.Party;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import com.quorum.tessera.test.PartyHelper;

public class AdminConfigIT {

    private final Client client = ClientBuilder.newClient();

    private final PartyHelper partyHelper = PartyHelper.create();

    @Test
    public void addPeer() {

        Party party = partyHelper.getParties().findAny().get();

        String url = "http://" + UUID.randomUUID().toString().replaceAll("-", "");

        Peer peer = new Peer(url);

        Response response =
                client.target(party.getAdminUri())
                        .path("config")
                        .path("peers")
                        .request(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .put(Entity.entity(peer, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(201);

        URI location = response.getLocation();

        Response queryResponse =
                client.target(location).request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get();

        assertThat(queryResponse.getStatus()).isEqualTo(200);

        assertThat(queryResponse.readEntity(Peer.class)).isEqualTo(peer);
    }
}
