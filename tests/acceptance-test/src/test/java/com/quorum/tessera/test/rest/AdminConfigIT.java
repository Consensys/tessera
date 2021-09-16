package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminConfigIT {

  private Client client;

  private PartyHelper partyHelper = PartyHelper.create();

  @Before
  public void beforeTest() {
    client = partyHelper.getParties().findAny().get().getRestClient();
  }

  @After
  public void afterTest() {
    client.close();
  }

  @Test
  public void addPeer() {

    Party party = partyHelper.getParties().findAny().get();

    String url = "http://" + UUID.randomUUID().toString().replaceAll("-", "");

    Peer peer = new Peer(url);

    Response response =
        client
            .target(party.getAdminUri())
            .path("config")
            .path("peers")
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .put(Entity.entity(peer, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(201);

    URI location = response.getLocation();

    Response queryResponse =
        client
            .target(location)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .get();

    assertThat(queryResponse.getStatus()).isEqualTo(200);

    assertThat(queryResponse.readEntity(Peer.class)).isEqualTo(peer);
  }
}
