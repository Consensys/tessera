package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PayloadEncoderImpl;
import com.quorum.tessera.p2p.resend.ResendRequest;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;

public class ResendIndividualIT {
  // Dont use for q2t for as unix ipc file support
  private final Client vanillaHttpOnlyClient = ClientBuilder.newClient();

  private static final String RESEND_PATH = "/resend";

  private static final PayloadEncoder ENCODER = new PayloadEncoderImpl();

  private String hash;

  private PartyHelper partyHelper = PartyHelper.create();

  private Party sender;

  private Party recipient;

  private static final String RESEND_INDIVIDUAL_VALUE = "INDIVIDUAL";

  @Before
  public void init() {

    sender = partyHelper.findByAlias("A");
    recipient = partyHelper.findByAlias("B");

    final Response response =
        sender
            .getRestClient()
            .target(sender.getQ2TUri())
            .path("/sendraw")
            .request()
            .header("c11n-from", sender.getPublicKey())
            .header("c11n-to", recipient.getPublicKey())
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

    // validate result
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);
    this.hash = response.readEntity(String.class);
  }

  @Test
  public void resendTransactionsForGivenKey() {

    final ResendRequest request = new ResendRequest();
    request.setType(RESEND_INDIVIDUAL_VALUE);
    request.setKey(this.hash);
    request.setPublicKey(recipient.getPublicKey());

    final Response response =
        vanillaHttpOnlyClient
            .target(sender.getP2PUri())
            .path(RESEND_PATH)
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final byte[] returnValue = response.readEntity(byte[].class);
    final EncodedPayload payload = ENCODER.decode(returnValue);

    assertThat(payload).isNotNull();
    assertThat(payload.getRecipientKeys()).hasSize(1);
    assertThat(payload.getRecipientKeys().get(0).encodeToBase64())
        .isEqualTo(recipient.getPublicKey());
    assertThat(payload.getSenderKey().encodeToBase64()).isEqualTo(sender.getPublicKey());
  }

  @Test
  public void resendTransactionWhereKeyIsSender() {
    final ResendRequest request = new ResendRequest();
    request.setType(RESEND_INDIVIDUAL_VALUE);
    request.setKey(this.hash);
    request.setPublicKey(sender.getPublicKey());

    final Response response =
        vanillaHttpOnlyClient
            .target(recipient.getP2PUri())
            .path(RESEND_PATH)
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final byte[] returnValue = response.readEntity(byte[].class);
    final EncodedPayload payload = ENCODER.decode(returnValue);

    assertThat(payload).isNotNull();
    assertThat(payload.getRecipientKeys().get(0).encodeToBase64())
        .isEqualTo(recipient.getPublicKey());
    assertThat(payload.getSenderKey().encodeToBase64()).isEqualTo(sender.getPublicKey());
  }

  @Test
  public void resendTransactionForIncorrectKey() {
    final ResendRequest request = new ResendRequest();
    request.setType(RESEND_INDIVIDUAL_VALUE);
    request.setKey(this.hash);

    Party anyOtherParty =
        partyHelper
            .getParties()
            .filter(p -> !p.equals(sender))
            .filter(p -> !p.equals(recipient))
            .findAny()
            .get();
    request.setPublicKey(anyOtherParty.getPublicKey());

    final Response response =
        vanillaHttpOnlyClient
            .target(recipient.getP2PUri())
            .path(RESEND_PATH)
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(500);
    assertThat(response.readEntity(String.class))
        .contains(
            "Recipient " + anyOtherParty.getPublicKey() + " is not a recipient of transaction");
  }

  @Test
  public void resendTransactionThatDoesntExist() {

    final String unknownHash =
        "2xTEBlTtYXSBXZD4jDDp83cVJbnkzP6PbUoUJx076BO/FSR75NXwDDpLDu3AIiDV1TlK8nGK4mlhsg4Xzpd5og==";

    final ResendRequest request = new ResendRequest();
    request.setType(RESEND_INDIVIDUAL_VALUE);
    request.setKey(unknownHash);
    request.setPublicKey(
        partyHelper
            .getParties()
            .filter(p -> !p.equals(sender))
            .filter(p -> !p.equals(recipient))
            .findAny()
            .get()
            .getPublicKey());

    final Response response =
        vanillaHttpOnlyClient
            .target(recipient.getP2PUri())
            .path(RESEND_PATH)
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(response.readEntity(String.class))
        .contains("Message with hash " + unknownHash + " was not found");
  }
}
