package com.quorum.tessera.test.rest;

import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyGroupTestUtil {

    private final Client client = ClientBuilder.newClient();

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
                client.target(sender.getQ2TUri())
                        .path("/createPrivacyGroup")
                        .request()
                        .post(Entity.entity(json, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);

        return response.readEntity(String.class);
    }

    public String retrieve(String targetNode, String groupId) {

        JsonObject reqJson = Json.createObjectBuilder().add("privacyGroupId", groupId).build();

        final Response response =
                client.target(partyHelper.findByAlias(targetNode).getQ2TUri())
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

        final Response response =
                client.target(partyHelper.findByAlias(targetNode).getQ2TUri())
                        .path("/findPrivacyGroup")
                        .request()
                        .post(Entity.entity(json, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);

        return response.readEntity(String.class);
    }
}
