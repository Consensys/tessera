package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.ReceiveRequest;
import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.Base64;
import java.util.Optional;
import org.junit.Test;

public class SendReceiveBesuIT {

  private final Client client = ClientBuilder.newClient();

  private final PartyHelper partyHelper = PartyHelper.create();

  private RestUtils utils = new RestUtils();

  private final PrivacyGroupTestUtil privacyGroupTestUtil = new PrivacyGroupTestUtil();

  @Test
  public void sendAndReceivePrivacyGroup() {
    final Party a = partyHelper.findByAlias("A");
    final Party b = partyHelper.findByAlias("B");

    final String output = privacyGroupTestUtil.create("A", "B");
    final JsonObject jsonObj = Json.createReader(new StringReader(output)).readObject();
    final String groupId = jsonObj.getString("privacyGroupId");

    byte[] transactionData = utils.createTransactionData();

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPrivacyGroupId(groupId);
    sendRequest.setPayload(transactionData);

    final Response response =
        client
            .target(partyHelper.findByAlias("A").getQ2TUri())
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    final SendResponse result = response.readEntity(SendResponse.class);
    final String hash = result.getKey();
    assertThat(hash).isNotNull().isNotBlank();

    // Hash length = 32 bytes
    assertThat(Base64.getDecoder().decode(hash)).hasSize(32);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    ReceiveRequest receiveRequest = new ReceiveRequest();
    receiveRequest.setKey(hash);

    final Response receiveResponse =
        client
            .target(a.getQ2TUri())
            .path("/receive")
            .request()
            .post(Entity.entity(receiveRequest, MediaType.APPLICATION_JSON));

    // validate result
    assertThat(receiveResponse).isNotNull();
    assertThat(receiveResponse.getStatus()).isEqualTo(200);

    final ReceiveResponse receiveResult = receiveResponse.readEntity(ReceiveResponse.class);

    assertThat(receiveResult.getPayload()).isEqualTo(transactionData);
    assertThat(receiveResult.getSenderKey()).isEqualTo(a.getPublicKey());
    assertThat(receiveResult.getPrivacyGroupId()).isEqualTo(groupId);

    final Response receiveResponseOnB =
        client
            .target(b.getQ2TUri())
            .path("/receive")
            .request()
            .post(Entity.entity(receiveRequest, MediaType.APPLICATION_JSON));

    // validate result
    assertThat(receiveResponseOnB).isNotNull();
    assertThat(receiveResponseOnB.getStatus()).isEqualTo(200);

    final ReceiveResponse receiveResultOnB = receiveResponseOnB.readEntity(ReceiveResponse.class);
    assertThat(receiveResultOnB.getPayload()).isEqualTo(transactionData);
    assertThat(receiveResultOnB.getSenderKey()).isEqualTo(a.getPublicKey());
    assertThat(receiveResultOnB.getPrivacyGroupId()).isEqualTo(groupId);
  }

  @Test
  public void sendAndReceivePrivacyFor() throws InterruptedException {

    final Party a = partyHelper.findByAlias("A");
    final Party d = partyHelper.findByAlias("D");

    byte[] transactionData = utils.createTransactionData();

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(transactionData);
    sendRequest.setTo(d.getPublicKey());

    final Response response =
        client
            .target(a.getQ2TUri())
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final SendResponse result = response.readEntity(SendResponse.class);
    final String hash = result.getKey();

    // Hash length = 32 bytes
    assertThat(Base64.getDecoder().decode(hash)).hasSize(32);

    String findOutput = privacyGroupTestUtil.find("A", "A", "D");
    final JsonArray json = Json.createReader(new StringReader(findOutput)).readArray();

    Optional<JsonObject> legacyGroup =
        json.getValuesAs(JsonObject.class).stream()
            .filter(v -> v.getString("type").equals("LEGACY"))
            .findAny();

    // Legacy privacy group was created
    assertThat(legacyGroup).isPresent();
    final String groupId = legacyGroup.get().getString("privacyGroupId");

    ReceiveRequest receiveRequest = new ReceiveRequest();
    receiveRequest.setKey(hash);

    final Response receiveResponse =
        client
            .target(a.getQ2TUri())
            .path("/receive")
            .request()
            .post(Entity.entity(receiveRequest, MediaType.APPLICATION_JSON));

    // validate result
    assertThat(receiveResponse).isNotNull();
    assertThat(receiveResponse.getStatus()).isEqualTo(200);

    final ReceiveResponse receiveResult = receiveResponse.readEntity(ReceiveResponse.class);

    assertThat(receiveResult.getPayload()).isEqualTo(transactionData);
    assertThat(receiveResult.getSenderKey()).isEqualTo(a.getPublicKey());
    assertThat(receiveResult.getPrivacyGroupId()).isEqualTo(groupId);

    final Response receiveResponseOnB =
        client
            .target(d.getQ2TUri())
            .path("/receive")
            .request()
            .post(Entity.entity(receiveRequest, MediaType.APPLICATION_JSON));

    // validate result
    assertThat(receiveResponseOnB).isNotNull();
    assertThat(receiveResponseOnB.getStatus()).isEqualTo(200);

    final ReceiveResponse receiveResultOnB = receiveResponseOnB.readEntity(ReceiveResponse.class);
    assertThat(receiveResultOnB.getPayload()).isEqualTo(transactionData);
    assertThat(receiveResultOnB.getSenderKey()).isEqualTo(a.getPublicKey());
    assertThat(receiveResultOnB.getPrivacyGroupId()).isEqualTo(groupId);
  }
}
