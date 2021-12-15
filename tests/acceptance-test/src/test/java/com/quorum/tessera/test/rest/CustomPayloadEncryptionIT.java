package com.quorum.tessera.test.rest;

import static com.quorum.tessera.version.MultiTenancyVersion.MIME_TYPE_JSON_2_1;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.PayloadEncryptResponse;
import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import suite.NodeAlias;

@RunWith(Parameterized.class)
public class CustomPayloadEncryptionIT {

  private final PartyHelper partyHelper = PartyHelper.create();

  private String mediaType;

  public CustomPayloadEncryptionIT(final String mediaType) {
    this.mediaType = mediaType;
  }

  @Parameterized.Parameters
  public static List<String> params() {
    return List.of(MediaType.APPLICATION_JSON, MIME_TYPE_JSON_2_1);
  }

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
            .post(Entity.entity(sendRequest, mediaType));

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
            .post(Entity.entity(sendRequest, mediaType));

    assertThat(encryptResult.getStatus()).isEqualTo(200);
    final PayloadEncryptResponse payloadEncryptResponse =
        encryptResult.readEntity(PayloadEncryptResponse.class);

    // decrypt it again with the sender
    final Response decryptResultForSender =
        sender
            .getRestClientWebTarget()
            .path("/encodedpayload/decrypt")
            .request()
            .post(Entity.entity(payloadEncryptResponse, mediaType));
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
            .post(Entity.entity(payloadEncryptResponse, mediaType));
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
            .post(Entity.entity(sendRequest, mediaType));

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
            .post(Entity.entity(payloadEncryptResponse, mediaType));
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
            .post(Entity.entity(sendRequest, mediaType));

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
            .post(Entity.entity(payloadEncryptResponse, mediaType));
    assertThat(decryptResultForRecipient.getStatus()).isEqualTo(500);
  }
}
