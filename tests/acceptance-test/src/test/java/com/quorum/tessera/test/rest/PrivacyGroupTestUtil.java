package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.stream.Stream;

public class PrivacyGroupTestUtil {

  private PartyHelper partyHelper = PartyHelper.create();

  public String create(String... aliases) {
    JsonArrayBuilder members = Json.createArrayBuilder();
    Stream.of(aliases).map(partyHelper::findByAlias).map(Party::getPublicKey).forEach(members::add);

    Party sender = partyHelper.findByAlias(aliases[0]);

    JsonObject json =
        Json.createObjectBuilder()
            .add("addresses", members)
            .add("from", sender.getPublicKey())
            .add("name", "Organisation " + Arrays.toString(aliases))
            .add("description", "Contains members of Organisation " + Arrays.toString(aliases))
            .build();

    final Response response =
        sender
            .getRestClient()
            .target(sender.getQ2TUri())
            .path("/createPrivacyGroup")
            .request()
            .post(Entity.entity(json, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(200);

    return response.readEntity(String.class);
  }

  public String retrieve(String targetNode, String groupId) {

    JsonObject reqJson = Json.createObjectBuilder().add("privacyGroupId", groupId).build();
    Party node = partyHelper.findByAlias(targetNode);
    final Response response =
        node.getRestClient()
            .target(node.getQ2TUri())
            .path("/retrievePrivacyGroup")
            .request()
            .post(Entity.entity(reqJson, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(200);

    return response.readEntity(String.class);
  }

  public String find(String targetNode, String... aliases) {

    JsonArrayBuilder members = Json.createArrayBuilder();
    Stream.of(aliases).map(partyHelper::findByAlias).map(Party::getPublicKey).forEach(members::add);

    JsonObject json = Json.createObjectBuilder().add("addresses", members).build();
    Party node = partyHelper.findByAlias(targetNode);
    final Response response =
        node.getRestClient()
            .target(node.getQ2TUri())
            .path("/findPrivacyGroup")
            .request()
            .post(Entity.entity(json, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(200);

    return response.readEntity(String.class);
  }
}
