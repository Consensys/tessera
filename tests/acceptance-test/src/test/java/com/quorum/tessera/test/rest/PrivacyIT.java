package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.junit.*;
import suite.*;

public class PrivacyIT {

  private PartyHelper partyHelper = PartyHelper.create();

  @Test
  public void enhancedPrivacyTransactionsNotEnabled() {

    Party legacySender = partyHelper.findByAlias(NodeAlias.D);

    SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(new RestUtils().createTransactionData());
    sendRequest.setFrom(legacySender.getPublicKey());

    List<String> recipientList = List.of(partyHelper.findByAlias(NodeAlias.A).getPublicKey());

    sendRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
    sendRequest.setPrivacyFlag(PrivacyMode.PARTY_PROTECTION.getPrivacyFlag());
    sendRequest.setAffectedContractTransactions(new String[0]);

    Response response =
        legacySender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void targetedNodeDoesNotHaveEnhancedPrivacyEnabled() {

    Party sender = partyHelper.findByAlias(NodeAlias.A);

    SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(new RestUtils().createTransactionData());
    sendRequest.setFrom(sender.getPublicKey());

    List<String> recipientList = List.of(partyHelper.findByAlias(NodeAlias.D).getPublicKey());

    sendRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
    sendRequest.setPrivacyFlag(PrivacyMode.PARTY_PROTECTION.getPrivacyFlag());
    sendRequest.setAffectedContractTransactions(new String[0]);

    Response response =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  public void oneOfTargetedRecipientsDoesNotHaveEnhancedPrivacyEnabled() {

    Party sender = partyHelper.findByAlias(NodeAlias.A);

    SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(new RestUtils().createTransactionData());
    sendRequest.setFrom(sender.getPublicKey());

    List<String> recipientList =
        List.of(
            partyHelper.findByAlias(NodeAlias.C).getPublicKey(),
            partyHelper.findByAlias(NodeAlias.D).getPublicKey());

    sendRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
    sendRequest.setPrivacyFlag(PrivacyMode.PARTY_PROTECTION.getPrivacyFlag());
    sendRequest.setAffectedContractTransactions(new String[0]);

    Response response =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  public void sendPSVTransactionWithoutExecHashWillBeRejected() {

    Party sender = partyHelper.findByAlias(NodeAlias.A);

    SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(new RestUtils().createTransactionData());
    sendRequest.setFrom(sender.getPublicKey());

    List<String> recipientList = List.of(partyHelper.findByAlias(NodeAlias.B).getPublicKey());

    sendRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
    sendRequest.setPrivacyFlag(PrivacyMode.PRIVATE_STATE_VALIDATION.getPrivacyFlag());
    sendRequest.setAffectedContractTransactions(new String[0]);

    Response response =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void sendTransactionsWithFlagMismatched() {

    Party sender = partyHelper.findByAlias(NodeAlias.A);

    final String originalHash = sendContractCreationTransaction(PrivacyMode.PARTY_PROTECTION);

    SendRequest secondRequest = new SendRequest();
    secondRequest.setPayload(new RestUtils().createTransactionData());
    secondRequest.setFrom(sender.getPublicKey());
    List<String> recipientList = List.of(partyHelper.findByAlias(NodeAlias.B).getPublicKey());
    secondRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
    secondRequest.setPrivacyFlag(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());
    secondRequest.setAffectedContractTransactions(originalHash);

    Response secondResponse =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(secondRequest, MediaType.APPLICATION_JSON));

    assertThat(secondResponse.getStatus()).isEqualTo(403);
  }

  @Test
  public void sendPSVTransactionsWithRecipientsMismatched() {

    Party sender = partyHelper.findByAlias(NodeAlias.A);

    final String originalHash =
        sendContractCreationTransaction(PrivacyMode.PRIVATE_STATE_VALIDATION);

    SendRequest secondRequest = new SendRequest();
    secondRequest.setPayload(new RestUtils().createTransactionData());
    secondRequest.setFrom(sender.getPublicKey());
    List<String> anotherList =
        List.of(
            partyHelper.findByAlias(NodeAlias.B).getPublicKey(),
            partyHelper.findByAlias(NodeAlias.C).getPublicKey());
    secondRequest.setTo(anotherList.toArray(new String[anotherList.size()]));
    secondRequest.setPrivacyFlag(PrivacyMode.PRIVATE_STATE_VALIDATION.getPrivacyFlag());
    secondRequest.setAffectedContractTransactions(originalHash);
    secondRequest.setExecHash("execHash");

    Response secondResponse =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(secondRequest, MediaType.APPLICATION_JSON));

    assertThat(secondResponse.getStatus()).isEqualTo(403);
  }

  @Test
  public void updateExistingContractByParticipant() {

    final String originalHash = sendContractCreationTransaction(PrivacyMode.PARTY_PROTECTION);

    Party sender = partyHelper.findByAlias(NodeAlias.B);

    SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(new RestUtils().createTransactionData());
    sendRequest.setFrom(sender.getPublicKey());
    List<String> recipientList = List.of(partyHelper.findByAlias(NodeAlias.A).getPublicKey());
    sendRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
    sendRequest.setPrivacyFlag(PrivacyMode.PARTY_PROTECTION.getPrivacyFlag());
    sendRequest.setAffectedContractTransactions(originalHash);

    Response response =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(201);
  }

  @Test
  public void updateExistingContractByNonParticipant() {

    final String originalHash = sendContractCreationTransaction(PrivacyMode.PARTY_PROTECTION);

    Party sender = partyHelper.findByAlias(NodeAlias.C);

    SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(new RestUtils().createTransactionData());
    sendRequest.setFrom(sender.getPublicKey());
    List<String> recipientList = List.of(partyHelper.findByAlias(NodeAlias.B).getPublicKey());
    sendRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
    sendRequest.setPrivacyFlag(PrivacyMode.PARTY_PROTECTION.getPrivacyFlag());
    sendRequest.setAffectedContractTransactions(originalHash);

    Response response =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(403);
  }

  private String sendContractCreationTransaction(PrivacyMode privacyMode) {

    Party sender = partyHelper.findByAlias(NodeAlias.A);

    SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(new RestUtils().createTransactionData());
    sendRequest.setFrom(sender.getPublicKey());

    List<String> recipientList = List.of(partyHelper.findByAlias(NodeAlias.B).getPublicKey());

    sendRequest.setTo(recipientList.toArray(new String[recipientList.size()]));
    sendRequest.setPrivacyFlag(privacyMode.getPrivacyFlag());
    sendRequest.setAffectedContractTransactions(new String[0]);
    if (privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
      sendRequest.setExecHash("execHash");
    }

    Response response =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(201);
    final SendResponse result = response.readEntity(SendResponse.class);

    return result.getKey();
  }
}
