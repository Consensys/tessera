package com.quorum.tessera.test.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import suite.NodeAlias;

/** retrieve tx with hash retrieve tx with hash and sender retrieve hash that doesn't exist */
public class ReceiveIT {

  private static final String RECEIVE_PATH = "transaction";

  private PartyHelper partyHelper = PartyHelper.create();

  private byte[] transactionData = UUID.randomUUID().toString().getBytes();

  private String encodedHash;

  private String encodedSender;

  private String encodedRecipientOne;

  private Party partyOne;

  private Party partyTwo;

  private Party partyThee;

  // Persist a single transaction that can be used later
  @Before
  public void beforeTest() throws UnsupportedEncodingException {

    partyOne = partyHelper.findByAlias(NodeAlias.A);
    partyTwo = partyHelper.findByAlias(NodeAlias.B);
    partyThee = partyHelper.findByAlias(NodeAlias.C);

    SendRequest sendRequest = new SendRequest();
    sendRequest.setFrom(partyOne.getPublicKey());
    sendRequest.setTo(partyTwo.getPublicKey());
    sendRequest.setPayload(transactionData);

    final Response response =
        partyOne
            .getRestClient()
            .target(partyOne.getQ2TUri())
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(201);

    final SendResponse result = response.readEntity(SendResponse.class);

    final String hash = result.getKey();

    this.encodedHash = URLEncoder.encode(hash, UTF_8.toString());
    this.encodedSender = URLEncoder.encode(partyOne.getPublicKey(), UTF_8.toString());
    this.encodedRecipientOne = URLEncoder.encode(partyThee.getPublicKey(), UTF_8.toString());
  }

  @Test
  public void fetchExistingTransactionUsingOwnKey() {

    final Response response =
        partyOne
            .getRestClient()
            .target(partyOne.getQ2TUri())
            .path(RECEIVE_PATH + "/" + this.encodedHash)
            .request()
            .buildGet()
            .property("to", this.encodedSender)
            .invoke();

    // validate result
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final ReceiveResponse result = response.readEntity(ReceiveResponse.class);

    assertThat(result.getPayload()).isEqualTo(transactionData);
  }

  @Test
  public void fetchExistingTransactionUsingRecipientKey() throws Exception {

    final Response response =
        partyOne
            .getRestClient()
            .target(partyOne.getQ2TUri())
            .path(RECEIVE_PATH)
            .path(encodedHash)
            .request()
            .property("to", this.encodedRecipientOne)
            .get();

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final ReceiveResponse result = response.readEntity(ReceiveResponse.class);

    assertThat(result.getPayload()).isEqualTo(transactionData);
  }

  @Test
  public void fetchExistingTransactionNotUsingKey() throws UnsupportedEncodingException {

    final Response response =
        partyOne
            .getRestClient()
            .target(partyOne.getQ2TUri())
            .path(RECEIVE_PATH)
            .path(encodedHash)
            .request()
            .buildGet()
            .invoke();

    // validate result
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final ReceiveResponse result = response.readEntity(ReceiveResponse.class);

    assertThat(result.getPayload()).isEqualTo(transactionData);
  }

  @Test
  public void fetchNonexistantTransactionFails() {

    final Response response =
        partyOne
            .getRestClient()
            .target(partyOne.getQ2TUri())
            .path(RECEIVE_PATH)
            .path("invalidhashvalue")
            .request()
            .get();

    // validate result
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(404);

    final String result = response.readEntity(String.class);
    assertThat(result).isEqualTo("Message with hash invalidhashvalue was not found");
  }
}
