package com.quorum.tessera.q2t;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.api.SendSignedRequest;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.transaction.ReceiveResponse;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResource4Test {

  private TransactionManager transactionManager;

  private PrivacyGroupManager privacyGroupManager;

  private TransactionResource4 transactionResource;

  @Before
  public void beforeTest() throws Exception {
    this.transactionManager = mock(TransactionManager.class);
    this.privacyGroupManager = mock(PrivacyGroupManager.class);

    transactionResource = new TransactionResource4(transactionManager, privacyGroupManager);
  }

  @After
  public void afterTest() throws Exception {
    verifyNoMoreInteractions(transactionManager, privacyGroupManager);
  }

  @Test
  public void sendWithMandatoryRecipients() {
    final Base64.Encoder base64Encoder = Base64.getEncoder();

    final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
    final String base64Hash =
      "yKNxAAPdBMiEZFkyQifH1PShwHTHTdE92T3hAfSQ3RtGce9IB8jrsrXxGuCe+Vu3Wyv2zgSbUnt+QBN2Rf48qQ==";

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));
    sendRequest.setTo(base64Key);
    sendRequest.setPrivacyFlag(2);
    sendRequest.setMandatoryRecipients(new String[] {base64Key});
    sendRequest.setAffectedContractTransactions(base64Hash);

    final PublicKey sender = mock(PublicKey.class);
    when(transactionManager.defaultPublicKey()).thenReturn(sender);

    final com.quorum.tessera.transaction.SendResponse sendResponse =
      mock(com.quorum.tessera.transaction.SendResponse.class);

    final MessageHash messageHash = mock(MessageHash.class);

    final byte[] txnData = "TxnData".getBytes();
    when(messageHash.getHashBytes()).thenReturn(txnData);

    when(sendResponse.getTransactionHash()).thenReturn(messageHash);

    when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class)))
      .thenReturn(sendResponse);

    final Response result = transactionResource.send(sendRequest);

    assertThat(result.getStatus()).isEqualTo(201);

    assertThat(result.getLocation().getPath())
      .isEqualTo("transaction/" + base64Encoder.encodeToString(txnData));
    SendResponse resultSendResponse = (SendResponse) result.getEntity();
    assertThat(resultSendResponse.getKey());

    ArgumentCaptor<com.quorum.tessera.transaction.SendRequest> argumentCaptor =
      ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendRequest.class);

    verify(transactionManager).send(argumentCaptor.capture());
    verify(transactionManager).defaultPublicKey();

    com.quorum.tessera.transaction.SendRequest businessObject = argumentCaptor.getValue();

    assertThat(businessObject).isNotNull();
    assertThat(businessObject.getPayload()).isEqualTo(sendRequest.getPayload());
    assertThat(businessObject.getSender()).isEqualTo(sender);
    assertThat(businessObject.getRecipients()).hasSize(1);
    assertThat(businessObject.getRecipients().get(0).encodeToBase64()).isEqualTo(base64Key);

    assertThat(businessObject.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(businessObject.getAffectedContractTransactions()).hasSize(1);
    final MessageHash hash = businessObject.getAffectedContractTransactions().iterator().next();
    assertThat(Base64.getEncoder().encodeToString(hash.getHashBytes())).isEqualTo(base64Hash);
    assertThat(businessObject.getMandatoryRecipients().iterator().next().encodeToBase64())
      .isEqualTo(base64Key);
  }

  @Test
  public void receiveMandatoryRecipients() {
    final PublicKey senderPublicKey = PublicKey.from("sender".getBytes());
    final PublicKey mandatoryKey = PublicKey.from("auditor".getBytes());

    ReceiveResponse response =
      ReceiveResponse.Builder.create()
        .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
        .withAffectedTransactions(Set.of())
        .withUnencryptedTransactionData("Success".getBytes())
        .withManagedParties(Set.of(senderPublicKey))
        .withSender(senderPublicKey)
        .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
        .withMandatoryRecipients(Set.of(mandatoryKey))
        .build();

    when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class)))
      .thenReturn(response);

    String transactionHash = Base64.getEncoder().encodeToString("transactionHash".getBytes());

    Response result = transactionResource.receive(transactionHash, null, Boolean.FALSE.toString());
    assertThat(result.getStatus()).isEqualTo(200);

    com.quorum.tessera.api.ReceiveResponse resultResponse =
      com.quorum.tessera.api.ReceiveResponse.class.cast(result.getEntity());

    assertThat(resultResponse.getPrivacyFlag())
      .isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS.getPrivacyFlag());
    assertThat(resultResponse.getAffectedContractTransactions()).isNullOrEmpty();
    assertThat(resultResponse.getPayload()).isEqualTo("Success".getBytes());
    assertThat(resultResponse.getManagedParties())
      .containsExactlyInAnyOrder(senderPublicKey.encodeToBase64());
    assertThat(resultResponse.getSenderKey()).isEqualTo(senderPublicKey.encodeToBase64());
    assertThat(resultResponse.getPrivacyGroupId())
      .isEqualTo(PublicKey.from("group".getBytes()).encodeToBase64());
    assertThat(resultResponse.getMandatoryRecipients()[0]).isEqualTo(mandatoryKey.encodeToBase64());

    verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
  }

  @Test
  public void sendSignedTransactionWithMandatoryRecipients() {
    final PublicKey sender =
      PublicKey.from(Base64.getDecoder().decode("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc="));

    com.quorum.tessera.transaction.SendResponse sendResponse =
      mock(com.quorum.tessera.transaction.SendResponse.class);

    byte[] transactionHashData = "I Love Sparrows".getBytes();
    final String base64EncodedTransactionHAshData =
      Base64.getEncoder().encodeToString(transactionHashData);
    MessageHash transactionHash = mock(MessageHash.class);
    when(transactionHash.getHashBytes()).thenReturn(transactionHashData);

    when(sendResponse.getTransactionHash()).thenReturn(transactionHash);
    when(sendResponse.getSender()).thenReturn(sender);

    when(transactionManager.sendSignedTransaction(
      any(com.quorum.tessera.transaction.SendSignedRequest.class)))
      .thenReturn(sendResponse);

    final String base64AffectedHash1 = Base64.getEncoder().encodeToString("aHash1".getBytes());
    final String base64AffectedHash2 = Base64.getEncoder().encodeToString("aHash2".getBytes());

    SendSignedRequest sendSignedRequest = new SendSignedRequest();
    sendSignedRequest.setHash("SOMEDATA".getBytes());
    sendSignedRequest.setTo("recipient1", "recipient2");
    sendSignedRequest.setPrivacyFlag(2);
    sendSignedRequest.setAffectedContractTransactions(base64AffectedHash1, base64AffectedHash2);
    sendSignedRequest.setMandatoryRecipients("recipient2");

    Response result = transactionResource.sendSignedTransaction(sendSignedRequest);

    assertThat(result.getStatus()).isEqualTo(201);

    SendResponse resultResponse = (SendResponse) result.getEntity();

    assertThat(resultResponse.getKey()).isEqualTo(base64EncodedTransactionHAshData);
    assertThat(resultResponse.getSenderKey()).isEqualTo(sender.encodeToBase64());

    assertThat(result.getLocation())
      .hasPath("transaction/".concat(base64EncodedTransactionHAshData));

    ArgumentCaptor<com.quorum.tessera.transaction.SendSignedRequest> argumentCaptor =
      ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendSignedRequest.class);

    verify(transactionManager).sendSignedTransaction(argumentCaptor.capture());

    com.quorum.tessera.transaction.SendSignedRequest obj = argumentCaptor.getValue();

    assertThat(obj).isNotNull();
    assertThat(obj.getSignedData()).isEqualTo("SOMEDATA".getBytes());
    assertThat(obj.getRecipients()).hasSize(2);
    assertThat(obj.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(obj.getAffectedContractTransactions().stream().map(MessageHash::toString))
      .hasSize(2)
      .containsExactlyInAnyOrder(base64AffectedHash1, base64AffectedHash2);
    assertThat(obj.getMandatoryRecipients()).hasSize(1);
  }
}
