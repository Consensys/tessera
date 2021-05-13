package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.PayloadEncryptResponse;
import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Test;
import suite.NodeAlias;

public class CustomPayloadEncryptionIT {

  private final PartyHelper partyHelper = PartyHelper.create();

  @Test
  public void createPayload() {
    final Party sender = partyHelper.findByAlias(NodeAlias.A);

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(Base64.getEncoder().encode("Test Payload".getBytes()));
    sendRequest.setTo(partyHelper.getParties().map(Party::getPublicKey).toArray(String[]::new));

    Response result =
        sender
            .getRestClientWebTarget()
            .path("/encodedpayload/create")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(result.getStatus()).isEqualTo(200);

    final PayloadEncryptResponse payloadEncryptResponse =
        result.readEntity(PayloadEncryptResponse.class);
    assertThat(Base64.getEncoder().encodeToString(payloadEncryptResponse.getSenderKey()))
        .isEqualTo(sender.getPublicKey());
    assertThat(payloadEncryptResponse.getCipherText()).isNotEmpty();
    assertThat(payloadEncryptResponse.getCipherTextNonce()).isNotEmpty();
    assertThat(payloadEncryptResponse.getRecipientBoxes()).hasSize(4);
    assertThat(payloadEncryptResponse.getRecipientNonce()).isNotEmpty();
    assertThat(payloadEncryptResponse.getRecipientKeys())
        .hasSize(4)
        .containsExactlyInAnyOrder(
            partyHelper
                .getParties()
                .map(Party::getPublicKey)
                .map(Base64.getDecoder()::decode)
                .toArray(byte[][]::new));
    assertThat(payloadEncryptResponse.getPrivacyMode()).isEqualTo(0);
    assertThat(payloadEncryptResponse.getAffectedContractTransactions()).isEmpty();
    assertThat(payloadEncryptResponse.getExecHash()).isEmpty();
  }

  @Test
  public void createAndDecryptPayload() {
    final Party sender = partyHelper.findByAlias(NodeAlias.A);
    final Party recipient = partyHelper.findByAlias(NodeAlias.B);

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(Base64.getEncoder().encode("Test Payload".getBytes()));
    sendRequest.setTo(recipient.getPublicKey());

    final Response encryptResult =
        sender
            .getRestClientWebTarget()
            .path("/encodedpayload/create")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(encryptResult.getStatus()).isEqualTo(200);
    final PayloadEncryptResponse payloadEncryptResponse =
        encryptResult.readEntity(PayloadEncryptResponse.class);

    // decrypt it again with the sender
    final Response decryptResultForSender =
        sender
            .getRestClientWebTarget()
            .path("/encodedpayload/decrypt")
            .request()
            .post(Entity.entity(payloadEncryptResponse, MediaType.APPLICATION_JSON));
    final ReceiveResponse decryptedPayload =
        decryptResultForSender.readEntity(ReceiveResponse.class);
    assertThat(Base64.getDecoder().decode(decryptedPayload.getPayload()))
        .isEqualTo("Test Payload".getBytes());

    // decrypt it using the recipient
    final String firstRecipientInList =
        Base64.getEncoder().encodeToString(payloadEncryptResponse.getRecipientKeys().get(0));
    if (Objects.equals(firstRecipientInList, sender.getPublicKey())) {
      payloadEncryptResponse.getRecipientBoxes().remove(0);
    } else {
      payloadEncryptResponse.getRecipientBoxes().remove(1);
    }

    payloadEncryptResponse.setRecipientKeys(Collections.emptyList());

    final Response decryptResultForRecipient =
        recipient
            .getRestClientWebTarget()
            .path("/encodedpayload/decrypt")
            .request()
            .post(Entity.entity(payloadEncryptResponse, MediaType.APPLICATION_JSON));
    final ReceiveResponse decryptedPayloadForRecipient =
        decryptResultForRecipient.readEntity(ReceiveResponse.class);
    assertThat(Base64.getDecoder().decode(decryptedPayloadForRecipient.getPayload()))
        .isEqualTo("Test Payload".getBytes());
  }

  @Test
  public void payloadDecryptionFailsOnBadMessage() {
    final Party sender = partyHelper.findByAlias(NodeAlias.A);

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(Base64.getEncoder().encode("Test Payload".getBytes()));

    Response result =
        sender
            .getRestClientWebTarget()
            .path("/encodedpayload/create")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(result.getStatus()).isEqualTo(200);

    final PayloadEncryptResponse payloadEncryptResponse =
        result.readEntity(PayloadEncryptResponse.class);

    // edit the cipher text to something rubbish, so it can't be decrypted
    payloadEncryptResponse.setCipherText("Unexpected data".getBytes());

    // attempt to decrypt it
    final Response decryptResultForSender =
        sender
            .getRestClientWebTarget()
            .path("/encodedpayload/decrypt")
            .request()
            .post(Entity.entity(payloadEncryptResponse, MediaType.APPLICATION_JSON));
    assertThat(decryptResultForSender.getStatus()).isEqualTo(500);
  }

  @Test
  public void payloadDecryptionFailsWithoutProperKeyAvailable() {
    final Party sender = partyHelper.findByAlias(NodeAlias.A);
    final Party recipient = partyHelper.findByAlias(NodeAlias.B);

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(Base64.getEncoder().encode("Test Payload".getBytes()));
    sendRequest.setTo(recipient.getPublicKey());

    final Response encryptResult =
        sender
            .getRestClientWebTarget()
            .path("/encodedpayload/create")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(encryptResult.getStatus()).isEqualTo(200);
    final PayloadEncryptResponse payloadEncryptResponse =
        encryptResult.readEntity(PayloadEncryptResponse.class);

    // purposefully remove the wrong box so it can't be decrypted
    // since the key that corresponds to the remaining box isn't on that node
    final String firstRecipientInList =
        Base64.getEncoder().encodeToString(payloadEncryptResponse.getRecipientKeys().get(0));
    if (Objects.equals(firstRecipientInList, sender.getPublicKey())) {
      payloadEncryptResponse.getRecipientBoxes().remove(1);
    } else {
      payloadEncryptResponse.getRecipientBoxes().remove(0);
    }

    payloadEncryptResponse.setRecipientKeys(Collections.emptyList());

    final Response decryptResultForRecipient =
        recipient
            .getRestClientWebTarget()
            .path("/encodedpayload/decrypt")
            .request()
            .post(Entity.entity(payloadEncryptResponse, MediaType.APPLICATION_JSON));
    assertThat(decryptResultForRecipient.getStatus()).isEqualTo(500);
  }
}
