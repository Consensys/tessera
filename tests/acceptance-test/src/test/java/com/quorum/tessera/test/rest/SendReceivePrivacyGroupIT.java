package com.quorum.tessera.test.rest;

import static com.quorum.tessera.version.MultiTenancyVersion.MIME_TYPE_JSON_2_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import org.junit.Test;
import suite.NodeAlias;

public class SendReceivePrivacyGroupIT {

  private final PartyHelper partyHelper = PartyHelper.create();

  private RestUtils utils = new RestUtils();

  private final PrivacyGroupTestUtil privacyGroupTestUtil = new PrivacyGroupTestUtil();

  @Test
  public void sendTransactionToPrivacyGroup() throws UnsupportedEncodingException {

    final Party a = partyHelper.findByAlias(NodeAlias.A);
    final Party b = partyHelper.findByAlias(NodeAlias.B);

    final String output = privacyGroupTestUtil.create("A", "B");
    final JsonObject jsonObj = Json.createReader(new StringReader(output)).readObject();
    final String groupId = jsonObj.getString("privacyGroupId");

    byte[] transactionData = utils.createTransactionData();

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPrivacyGroupId(groupId);
    sendRequest.setPayload(transactionData);

    final Response response =
        a.getRestClient()
            .target(a.getQ2TUri())
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    final SendResponse result = response.readEntity(SendResponse.class);
    final String hash = result.getKey();

    // Hash length should be 64 bytes
    assertThat(Base64.getDecoder().decode(hash)).hasSize(64);

    final String encodedHash = URLEncoder.encode(hash, UTF_8.toString());
    assertThat(hash).isNotNull().isNotBlank();

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(201);

    final Response receiveResponse =
        a.getRestClient()
            .target(a.getQ2TUri())
            .path("/transaction")
            .path(encodedHash)
            .request()
            .accept(MIME_TYPE_JSON_2_1)
            .buildGet()
            .invoke();

    // validate result
    assertThat(receiveResponse).isNotNull();
    assertThat(receiveResponse.getStatus()).isEqualTo(200);

    final ReceiveResponse receiveResult = receiveResponse.readEntity(ReceiveResponse.class);

    assertThat(receiveResult.getPayload()).isEqualTo(transactionData);
    assertThat(receiveResult.getManagedParties()).containsExactly(a.getPublicKey());
    assertThat(receiveResult.getSenderKey()).isEqualTo(a.getPublicKey());
    //        assertThat(receiveResult.getPrivacyGroupId()).isEqualTo(groupId);

    final Response receiveResponseOnB =
        b.getRestClient()
            .target(b.getQ2TUri())
            .path("/transaction")
            .path(encodedHash)
            .request()
            .accept(MIME_TYPE_JSON_2_1)
            .buildGet()
            .invoke();

    // validate result
    assertThat(receiveResponseOnB).isNotNull();
    assertThat(receiveResponseOnB.getStatus()).isEqualTo(200);

    final ReceiveResponse receiveResultOnB = receiveResponseOnB.readEntity(ReceiveResponse.class);
    assertThat(receiveResultOnB.getPayload()).isEqualTo(transactionData);
    assertThat(receiveResultOnB.getManagedParties()).containsExactly(b.getPublicKey());
    assertThat(receiveResultOnB.getSenderKey()).isEqualTo(a.getPublicKey());
    //        assertThat(receiveResultOnB.getPrivacyGroupId()).isEqualTo(groupId);
  }
}
