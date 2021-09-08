package com.quorum.tessera.test.rest;

import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import java.io.Reader;
import java.io.StringReader;
import java.util.Base64;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PushIT {

  private static final String MSG_BASE64 =
      "AAAAAAAAACDIZyOQXJmSVNNeufz5YiRjCJwDYQYGf3BkWRy0Bp3hfQAAAAAAAAATJ9Sb5lOtjzaZayBRFP9jOUDczAAAAAAAAAAYggoaH+1mIGV91rR0KkrM89Pizi0e6MYGAAAAAAAAAAEAAAAAAAAAMP+KGceAS1WXguC8E6lq4tvtBi2gJfMa0QZ6DAGBdBp3fDAdTq8tKOjstP1aNIFqvAAAAAAAAAAYM9xGQhx2/GYXetUmFZNMe9ED57Rh58MWAAAAAAAAAAA=";

  private static final String PUSH_PATH = "/push";

  private static final String ENCODED_HASH =
      "QrAgXFRrZ8V24or%2BBZueIdZ6JBl2WQrqZqmmyFh%2FatsXyVkr2aMNEvQh0AsJvzt12oDpNkKmIv0KSnzM2HZL1w%3D%3D";

  private final PartyHelper partyHelper = PartyHelper.create();

  private Party party;

  private byte[] message;

  @Before
  public void beforeTest() {
    party = partyHelper.getParties().findAny().get();

    this.message = Base64.getDecoder().decode(MSG_BASE64);

    // delete the tx if it exists, or do nothing if it doesn't
    party
        .getRestClient()
        .target(party.getQ2TUri())
        .path("transaction")
        .path(ENCODED_HASH)
        .request()
        .buildDelete()
        .invoke();
  }

  // TODO: Remove test or generate message rather than using fixtures.
  // This test breaks since changing  test key pairs to be generated.
  @org.junit.Ignore
  @Test
  public void storePayloadFromAnotherNode() {

    final Response pushReponse =
        party
            .getRestClient()
            .target(party.getP2PUri())
            .path(PUSH_PATH)
            .request()
            .post(Entity.entity(message, APPLICATION_OCTET_STREAM));

    assertThat(pushReponse).isNotNull();
    assertThat(pushReponse.getStatus()).isEqualTo(201);

    // retrieve that tx

    final Response retrieveResponse =
        party
            .getRestClient()
            .target(party.getQ2TUri())
            .path("/transaction/" + ENCODED_HASH)
            .request()
            .buildGet()
            .invoke();

    assertThat(retrieveResponse).isNotNull();
    assertThat(retrieveResponse.getStatus()).isEqualTo(200);

    final String result = retrieveResponse.readEntity(String.class);
    final Reader reader = new StringReader(result);
    final JsonObject jsonResult = Json.createReader(reader).readObject();
    assertThat(jsonResult).containsKeys("payload");
    assertThat(jsonResult.getString("payload")).isEqualTo("Zm9v");
  }

  // TODO: There needs to be a protocol change/ammendment
  // as 500 gives us false positives. We cant discriminate between error types
  @Ignore
  @Test
  public void storeCorruptedPayloadFails() {

    final byte[] badPayload =
        "this is a bad payload that does not conform to the expected byte array".getBytes();

    final Response pushReponse =
        party
            .getRestClient()
            .target(party.getP2PUri())
            .path(PUSH_PATH)
            .request()
            .post(Entity.entity(badPayload, APPLICATION_OCTET_STREAM));

    assertThat(pushReponse).isNotNull();
    // TODO: should be 400?
    assertThat(pushReponse.getStatus()).isEqualTo(500);
  }
}
