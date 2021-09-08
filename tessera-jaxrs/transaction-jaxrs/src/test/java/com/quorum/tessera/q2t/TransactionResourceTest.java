package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.*;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.UnsupportedEncodingException;
import java.util.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionResourceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResourceTest.class);

  private TransactionManager transactionManager;

  private TransactionResource transactionResource;

  private PrivacyGroupManager privacyGroupManager;

  @Before
  public void beforeTest() throws Exception {
    transactionManager = mock(TransactionManager.class);
    privacyGroupManager = mock(PrivacyGroupManager.class);
    transactionResource = new TransactionResource(transactionManager, privacyGroupManager);
  }

  @After
  public void afterTest() throws Exception {
    verifyNoMoreInteractions(transactionManager, privacyGroupManager);
  }

  @Test
  public void receiveFromParamsPrivateStateValidation() {

    com.quorum.tessera.transaction.ReceiveResponse response =
        mock(com.quorum.tessera.transaction.ReceiveResponse.class);
    when(response.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(response.getAffectedTransactions()).thenReturn(Collections.emptySet());
    when(response.getUnencryptedTransactionData()).thenReturn("Success".getBytes());
    when(response.getExecHash()).thenReturn("execHash".getBytes());

    when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class)))
        .thenReturn(response);

    String transactionHash = Base64.getEncoder().encodeToString("transactionHash".getBytes());

    Response result = transactionResource.receive(transactionHash, null, null);

    assertThat(result.getStatus()).isEqualTo(200);

    com.quorum.tessera.api.ReceiveResponse resultResponse =
        com.quorum.tessera.api.ReceiveResponse.class.cast(result.getEntity());

    assertThat(resultResponse.getExecHash()).isEqualTo("execHash");
    assertThat(resultResponse.getPrivacyFlag())
        .isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION.getPrivacyFlag());

    assertThat(resultResponse.getAffectedContractTransactions()).isNullOrEmpty();
    assertThat(resultResponse.getPayload()).isEqualTo("Success".getBytes());

    verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
  }

  @Test
  public void receiveFromParams() {

    com.quorum.tessera.transaction.ReceiveResponse response =
        mock(com.quorum.tessera.transaction.ReceiveResponse.class);
    when(response.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(response.getUnencryptedTransactionData()).thenReturn("Success".getBytes());

    when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class)))
        .thenReturn(response);

    String transactionHash = Base64.getEncoder().encodeToString("transactionHash".getBytes());

    Response result = transactionResource.receive(transactionHash, null, null);

    assertThat(result.getStatus()).isEqualTo(200);

    com.quorum.tessera.api.ReceiveResponse resultResponse =
        com.quorum.tessera.api.ReceiveResponse.class.cast(result.getEntity());
    assertThat(resultResponse.getExecHash()).isNull();
    assertThat(resultResponse.getPrivacyFlag())
        .isEqualTo(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());

    assertThat(resultResponse.getExecHash()).isNull();
    assertThat(resultResponse.getAffectedContractTransactions()).isNullOrEmpty();
    assertThat(resultResponse.getPayload()).isEqualTo("Success".getBytes());

    verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
  }

  @Test
  public void receiveRaw() {

    byte[] encodedPayload = Base64.getEncoder().encode("Payload".getBytes());
    com.quorum.tessera.transaction.ReceiveResponse receiveResponse =
        mock(com.quorum.tessera.transaction.ReceiveResponse.class);
    when(receiveResponse.getUnencryptedTransactionData()).thenReturn(encodedPayload);
    when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class)))
        .thenReturn(receiveResponse);

    final Response result = transactionResource.receiveRaw("", "");

    assertThat(result.getStatus()).isEqualTo(200);
    verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
  }

  @Test
  public void send() {

    final Base64.Encoder base64Encoder = Base64.getEncoder();

    final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));
    sendRequest.setTo(base64Key);

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
    // jersey.target("send").request().post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(result.getStatus()).isEqualTo(201);

    assertThat(result.getLocation().getPath())
        .isEqualTo("transaction/" + base64Encoder.encodeToString(txnData));
    SendResponse resultSendResponse = SendResponse.class.cast(result.getEntity());
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

    assertThat(businessObject.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(businessObject.getAffectedContractTransactions()).isEmpty();
    assertThat(businessObject.getExecHash()).isEmpty();
  }

  @Test
  public void sendWithPrivacy() {
    final Base64.Encoder base64Encoder = Base64.getEncoder();

    final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
    final String base64Hash =
        "yKNxAAPdBMiEZFkyQifH1PShwHTHTdE92T3hAfSQ3RtGce9IB8jrsrXxGuCe+Vu3Wyv2zgSbUnt+QBN2Rf48qQ==";

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));
    sendRequest.setTo(base64Key);
    sendRequest.setPrivacyFlag(3);
    sendRequest.setAffectedContractTransactions(base64Hash);
    sendRequest.setExecHash("executionHash");

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
    //  jersey.target("send").request().post(Entity.entity(sendRequest,
    // MediaType.APPLICATION_JSON));

    assertThat(result.getStatus()).isEqualTo(201);

    assertThat(result.getLocation().getPath())
        .isEqualTo("transaction/" + base64Encoder.encodeToString(txnData));
    SendResponse resultSendResponse = SendResponse.class.cast(result.getEntity());
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

    assertThat(businessObject.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(businessObject.getAffectedContractTransactions()).hasSize(1);
    final MessageHash hash = businessObject.getAffectedContractTransactions().iterator().next();
    assertThat(Base64.getEncoder().encodeToString(hash.getHashBytes())).isEqualTo(base64Hash);
    assertThat(businessObject.getExecHash()).isEqualTo("executionHash".getBytes());
  }

  @Test
  public void sendForRecipient() {

    final Base64.Encoder base64Encoder = Base64.getEncoder();

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));
    sendRequest.setTo(Base64.getEncoder().encodeToString("Mr Benn".getBytes()));
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
    //   jersey.target("send").request().post(Entity.entity(sendRequest,
    // MediaType.APPLICATION_JSON));

    assertThat(result.getStatus()).isEqualTo(201);

    assertThat(result.getLocation().getPath())
        .isEqualTo("transaction/" + base64Encoder.encodeToString(txnData));

    verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
    verify(transactionManager).defaultPublicKey();
  }

  @Test
  public void sendSignedTransactionWithRecipients() throws UnsupportedEncodingException {

    byte[] txnData = "KEY".getBytes();
    com.quorum.tessera.transaction.SendResponse sendResponse =
        mock(com.quorum.tessera.transaction.SendResponse.class);
    MessageHash messageHash = mock(MessageHash.class);
    when(messageHash.getHashBytes()).thenReturn(txnData);
    when(sendResponse.getTransactionHash()).thenReturn(messageHash);

    String recipentKey =
        "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=,QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";

    when(transactionManager.sendSignedTransaction(
            any(com.quorum.tessera.transaction.SendSignedRequest.class)))
        .thenReturn(sendResponse);

    Response result = transactionResource.sendSignedTransactionStandard(recipentKey, txnData);
    //                jersey.target("sendsignedtx")
    //                        .request()
    //                        .header("c11n-to", recipentKey)
    //                        .post(Entity.entity(txnData,
    // MediaType.APPLICATION_OCTET_STREAM_TYPE));

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString("KEY".getBytes()));

    ArgumentCaptor<com.quorum.tessera.transaction.SendSignedRequest> argumentCaptor =
        ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendSignedRequest.class);
    verify(transactionManager).sendSignedTransaction(argumentCaptor.capture());

    com.quorum.tessera.transaction.SendSignedRequest obj = argumentCaptor.getValue();

    assertThat(obj).isNotNull();
    assertThat(obj.getSignedData()).isEqualTo(txnData);
    assertThat(obj.getRecipients()).hasSize(2);
    assertThat(obj.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(obj.getAffectedContractTransactions()).isEmpty();
    assertThat(obj.getExecHash()).isEmpty();
  }

  @Test
  public void sendSignedTransactionEmptyRecipients() throws UnsupportedEncodingException {

    byte[] txnData = "KEY".getBytes();
    com.quorum.tessera.transaction.SendResponse sendResponse =
        mock(com.quorum.tessera.transaction.SendResponse.class);
    MessageHash messageHash = mock(MessageHash.class);
    when(messageHash.getHashBytes()).thenReturn(txnData);
    when(sendResponse.getTransactionHash()).thenReturn(messageHash);

    when(transactionManager.sendSignedTransaction(
            any(com.quorum.tessera.transaction.SendSignedRequest.class)))
        .thenReturn(sendResponse);

    StreamingOutput streamingOutput = output -> output.write("signedTxData".getBytes());

    Response result =
        transactionResource.sendSignedTransactionStandard("", "signedTxData".getBytes());
    //                jersey.target("sendsignedtx")
    //                        .request()
    //                        .header("c11n-to", "")
    //                        .post(Entity.entity(streamingOutput,
    // MediaType.APPLICATION_OCTET_STREAM_TYPE));

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString("KEY".getBytes()));
    verify(transactionManager)
        .sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class));
  }

  @Test
  public void sendSignedTransactionNullRecipients() throws UnsupportedEncodingException {

    byte[] txnData = "KEY".getBytes();
    com.quorum.tessera.transaction.SendResponse sendResponse =
        mock(com.quorum.tessera.transaction.SendResponse.class);
    MessageHash messageHash = mock(MessageHash.class);
    when(messageHash.getHashBytes()).thenReturn(txnData);
    when(sendResponse.getTransactionHash()).thenReturn(messageHash);

    when(transactionManager.sendSignedTransaction(
            any(com.quorum.tessera.transaction.SendSignedRequest.class)))
        .thenReturn(sendResponse);

    Response result =
        transactionResource.sendSignedTransactionStandard(null, "signedTxData".getBytes());
    //                jersey.target("sendsignedtx")
    //                        .request()
    //                        .header("c11n-to", null)
    //                        .post(Entity.entity("signedTxData".getBytes(),
    // MediaType.APPLICATION_OCTET_STREAM_TYPE));

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString("KEY".getBytes()));
    verify(transactionManager)
        .sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class));
  }

  @Test
  public void sendSignedTransaction() throws Exception {

    com.quorum.tessera.transaction.SendResponse sendResponse =
        mock(com.quorum.tessera.transaction.SendResponse.class);

    byte[] transactionHashData = "I Love Sparrows".getBytes();
    final String base64EncodedTransactionHAshData =
        Base64.getEncoder().encodeToString(transactionHashData);
    MessageHash transactionHash = mock(MessageHash.class);
    when(transactionHash.getHashBytes()).thenReturn(transactionHashData);

    when(sendResponse.getTransactionHash()).thenReturn(transactionHash);

    when(transactionManager.sendSignedTransaction(
            any(com.quorum.tessera.transaction.SendSignedRequest.class)))
        .thenReturn(sendResponse);

    SendSignedRequest sendSignedRequest = new SendSignedRequest();
    sendSignedRequest.setHash("SOMEDATA".getBytes());
    sendSignedRequest.setTo("recipient1", "recipient2");

    Response result = transactionResource.sendSignedTransactionEnhanced(sendSignedRequest);
    //                jersey.target("sendsignedtx")
    //                        .request()
    //                        .post(Entity.entity(sendSignedRequest,
    // MediaType.APPLICATION_JSON_TYPE));

    assertThat(result.getStatus()).isEqualTo(201);

    SendResponse resultResponse = SendResponse.class.cast(result.getEntity());

    assertThat(resultResponse.getKey()).isEqualTo(base64EncodedTransactionHAshData);

    assertThat(result.getLocation())
        .hasPath("transaction/".concat(base64EncodedTransactionHAshData));

    ArgumentCaptor<com.quorum.tessera.transaction.SendSignedRequest> argumentCaptor =
        ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendSignedRequest.class);

    verify(transactionManager).sendSignedTransaction(argumentCaptor.capture());

    com.quorum.tessera.transaction.SendSignedRequest obj = argumentCaptor.getValue();

    assertThat(obj).isNotNull();
    assertThat(obj.getSignedData()).isEqualTo("SOMEDATA".getBytes());
    assertThat(obj.getRecipients()).hasSize(2);
    assertThat(obj.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(obj.getAffectedContractTransactions()).isEmpty();
    assertThat(obj.getExecHash()).isEmpty();
  }

  @Test
  public void sendSignedTransactionWithPrivacy() {
    com.quorum.tessera.transaction.SendResponse sendResponse =
        mock(com.quorum.tessera.transaction.SendResponse.class);

    byte[] transactionHashData = "I Love Sparrows".getBytes();
    final String base64EncodedTransactionHAshData =
        Base64.getEncoder().encodeToString(transactionHashData);
    MessageHash transactionHash = mock(MessageHash.class);
    when(transactionHash.getHashBytes()).thenReturn(transactionHashData);

    when(sendResponse.getTransactionHash()).thenReturn(transactionHash);

    when(transactionManager.sendSignedTransaction(
            any(com.quorum.tessera.transaction.SendSignedRequest.class)))
        .thenReturn(sendResponse);

    final String base64AffectedHash1 = Base64.getEncoder().encodeToString("aHash1".getBytes());
    final String base64AffectedHash2 = Base64.getEncoder().encodeToString("aHash2".getBytes());

    SendSignedRequest sendSignedRequest = new SendSignedRequest();
    sendSignedRequest.setHash("SOMEDATA".getBytes());
    sendSignedRequest.setTo("recipient1", "recipient2");
    sendSignedRequest.setPrivacyFlag(3);
    sendSignedRequest.setAffectedContractTransactions(base64AffectedHash1, base64AffectedHash2);
    sendSignedRequest.setExecHash("execHash");

    Response result = transactionResource.sendSignedTransactionEnhanced(sendSignedRequest);
    //                jersey.target("sendsignedtx")
    //                        .request()
    //                        .post(Entity.entity(sendSignedRequest,
    // MediaType.APPLICATION_JSON_TYPE));

    assertThat(result.getStatus()).isEqualTo(201);

    SendResponse resultResponse = SendResponse.class.cast(result.getEntity());

    assertThat(resultResponse.getKey()).isEqualTo(base64EncodedTransactionHAshData);

    assertThat(result.getLocation())
        .hasPath("transaction/".concat(base64EncodedTransactionHAshData));

    ArgumentCaptor<com.quorum.tessera.transaction.SendSignedRequest> argumentCaptor =
        ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendSignedRequest.class);

    verify(transactionManager).sendSignedTransaction(argumentCaptor.capture());

    com.quorum.tessera.transaction.SendSignedRequest obj = argumentCaptor.getValue();

    assertThat(obj).isNotNull();
    assertThat(obj.getSignedData()).isEqualTo("SOMEDATA".getBytes());
    assertThat(obj.getRecipients()).hasSize(2);
    assertThat(obj.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(obj.getAffectedContractTransactions().stream().map(MessageHash::toString))
        .hasSize(2)
        .containsExactlyInAnyOrder(base64AffectedHash1, base64AffectedHash2);

    assertThat(obj.getExecHash()).isEqualTo("execHash".getBytes());
  }

  @Test
  public void sendRaw() throws UnsupportedEncodingException {

    byte[] txnData = "KEY".getBytes();
    com.quorum.tessera.transaction.SendResponse sendResponse =
        mock(com.quorum.tessera.transaction.SendResponse.class);
    MessageHash messageHash = mock(MessageHash.class);
    when(messageHash.getHashBytes()).thenReturn(txnData);
    when(sendResponse.getTransactionHash()).thenReturn(messageHash);

    when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));

    when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class)))
        .thenReturn(sendResponse);

    Response result = transactionResource.sendRaw("", "someone", "foo".getBytes());
    //                jersey.target("sendraw")
    //                        .request()
    //                        .header("c11n-from", "")
    //                        .header("c11n-to", "someone")
    //                        .post(Entity.entity("foo".getBytes(),
    // MediaType.APPLICATION_OCTET_STREAM_TYPE));

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString(txnData));
    verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
    verify(transactionManager).defaultPublicKey();
  }

  @Test
  public void sendRawEmptyRecipients() throws UnsupportedEncodingException {

    byte[] txnData = "KEY".getBytes();
    com.quorum.tessera.transaction.SendResponse sendResponse =
        mock(com.quorum.tessera.transaction.SendResponse.class);
    MessageHash messageHash = mock(MessageHash.class);
    when(messageHash.getHashBytes()).thenReturn(txnData);
    when(sendResponse.getTransactionHash()).thenReturn(messageHash);

    when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));
    when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class)))
        .thenReturn(sendResponse);

    Response result = transactionResource.sendRaw("", "", "foo".getBytes());
    //                jersey.target("sendraw")
    //                        .request()
    //                        .header("c11n-from", "")
    //                        .header("c11n-to", "")
    //                        .post(Entity.entity("foo".getBytes(),
    // MediaType.APPLICATION_OCTET_STREAM_TYPE));

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString(txnData));
    verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
    verify(transactionManager).defaultPublicKey();
  }

  @Test
  public void sendRawNullRecipient() throws UnsupportedEncodingException {

    byte[] txnData = "KEY".getBytes();
    com.quorum.tessera.transaction.SendResponse sendResponse =
        mock(com.quorum.tessera.transaction.SendResponse.class);
    MessageHash messageHash = mock(MessageHash.class);
    when(messageHash.getHashBytes()).thenReturn(txnData);
    when(sendResponse.getTransactionHash()).thenReturn(messageHash);
    when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));
    when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class)))
        .thenReturn(sendResponse);

    Response result = transactionResource.sendRaw("", "", "foo".getBytes());
    //                jersey.target("sendraw")
    //                        .request()
    //                        .header("c11n-from", "")
    //                        .header("c11n-to", null)
    //                        .post(Entity.entity("foo".getBytes(),
    // MediaType.APPLICATION_OCTET_STREAM_TYPE));

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString(txnData));
    verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
    verify(transactionManager).defaultPublicKey();
  }

  @Test
  public void delete() {

    DeleteRequest deleteRequest = new DeleteRequest();
    deleteRequest.setKey("KEY");

    Response response = transactionResource.delete(deleteRequest);
    //  jersey.target("delete").request().post(Entity.entity(deleteRequest,
    // MediaType.APPLICATION_JSON_TYPE));

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isEqualTo("Delete successful");
    verify(transactionManager).delete(any(MessageHash.class));
  }

  /*
  @Test
  public void validationSendPayloadCannotBeNullOrEmpty() {

      Collection<Entity> nullAndEmpty =
              List.of(
                      Entity.entity(null, MediaType.APPLICATION_OCTET_STREAM_TYPE),
                      Entity.entity(new byte[0], MediaType.APPLICATION_OCTET_STREAM_TYPE));

      Map<String, Collection<Entity>> pathToEntityMapping =
              Stream.of("sendsignedtx", "sendraw").collect(Collectors.toMap(s -> s, s -> nullAndEmpty));

      pathToEntityMapping.entrySet().stream()
              .forEach(
                      e -> {
                          e.getValue()
                                  .forEach(
                                          entity -> {
                                              Response response =
                                                      jersey.target(e.getKey())
                                                              .request()
                                                              .post(
                                                                      Entity.entity(
                                                                              null,
                                                                              MediaType
                                                                                      .APPLICATION_OCTET_STREAM_TYPE));
                                              assertThat(response.getStatus()).isEqualTo(400);
                                          });
                      });
  }
  */

  /*
  @Test
  public void validationReceiveIsRawMustBeBoolean() {
      Response response = jersey.target("transaction").path("MYHASH").queryParam("isRaw", "bogus").request().get();
      assertThat(response.getStatus()).isEqualTo(400);
  }
  */

  /*
  @Test
  public void receiveRawValidations() {
      assertThat(jersey.target("receiveraw").request().header("c11n-key", null).get().getStatus())
              .describedAs("key header cannot be null")
              .isEqualTo(400);

      assertThat(jersey.target("receiveraw").request().get().getStatus()).isEqualTo(400);

      assertThat(jersey.target("receiveraw").request().header("c11n-key", "notbase64").get().getStatus())
              .describedAs("key header must be valid base64")
              .isEqualTo(400);

      String validBase64Encoded = Base64.getEncoder().encodeToString("VALIDKEY".getBytes());
      assertThat(
                      jersey.target("receiveraw")
                              .request()
                              .header("c11n-key", validBase64Encoded)
                              .header("c11n-to", "notbase64")
                              .get()
                              .getStatus())
              .describedAs("to header must be valid base64")
              .isEqualTo(400);
  }
  */

}
