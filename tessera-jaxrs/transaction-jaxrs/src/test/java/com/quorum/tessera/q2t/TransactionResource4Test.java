package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.api.SendSignedRequest;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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

  @Test
  public void getMandatoryRecipients() {
    byte[] data = "DUMMY_HASH".getBytes();

    final String dummyPtmHash = Base64.getEncoder().encodeToString(data);

    PublicKey recipient = mock(PublicKey.class);
    when(recipient.encodeToBase64()).thenReturn("BASE64ENCODEDKEY");

    when(transactionManager.getMandatoryRecipients(any(MessageHash.class)))
        .thenReturn(Set.of(recipient));

    Response response = transactionResource.getMandatoryRecipients(dummyPtmHash);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isEqualTo("BASE64ENCODEDKEY");
    verify(transactionManager).getMandatoryRecipients(any(MessageHash.class));
  }
}
