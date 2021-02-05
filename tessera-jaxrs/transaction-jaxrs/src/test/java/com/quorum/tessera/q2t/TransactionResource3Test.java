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
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionResource3Test {

    private JerseyTest jersey;

    private TransactionManager transactionManager;

    private PrivacyGroupManager privacyGroupManager;

    @BeforeClass
    public static void setUpLoggers() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void onSetup() throws Exception {
        this.transactionManager = mock(TransactionManager.class);
        this.privacyGroupManager = mock(PrivacyGroupManager.class);

        final TransactionResource3 transactionResource =
                new TransactionResource3(transactionManager, privacyGroupManager);

        jersey =
                new JerseyTest() {
                    @Override
                    protected Application configure() {
                        forceSet(TestProperties.CONTAINER_PORT, "0");
                        enable(TestProperties.LOG_TRAFFIC);
                        enable(TestProperties.DUMP_ENTITY);
                        return new ResourceConfig().register(transactionResource);
                    }
                };

        jersey.setUp();
    }

    @After
    public void onTearDown() throws Exception {
        verifyNoMoreInteractions(transactionManager, privacyGroupManager);
        jersey.tearDown();
    }

    @Test
    public void receiveWithRecipient() {
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        String recipientKeyBase64 = Base64.getEncoder().encodeToString("recipient".getBytes());
        final PublicKey senderPublicKey = PublicKey.from("sender".getBytes());

        final ReceiveResponse receiveResponse =
                ReceiveResponse.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withUnencryptedTransactionData("Result".getBytes())
                        .withManagedParties(Set.of(senderPublicKey))
                        .withSender(senderPublicKey)
                        .build();
        when(transactionManager.receive(any())).thenReturn(receiveResponse);

        final Response result = jersey.target("transaction").path(key).request().header("to", recipientKeyBase64).get();

        assertThat(result.getStatus()).isEqualTo(200);

        com.quorum.tessera.api.ReceiveResponse resultResponse =
                result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);

        assertThat(resultResponse.getPrivacyFlag()).isEqualTo(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());
        assertThat(resultResponse.getPayload()).isEqualTo("Result".getBytes());
        assertThat(resultResponse.getManagedParties()).containsExactlyInAnyOrder(senderPublicKey.encodeToBase64());
        assertThat(resultResponse.getSenderKey()).isEqualTo(senderPublicKey.encodeToBase64());

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }

    @Test
    public void receivePrivateStateValidation() {
        final PublicKey senderPublicKey = PublicKey.from("sender".getBytes());

        ReceiveResponse response =
                ReceiveResponse.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withAffectedTransactions(Set.of())
                        .withUnencryptedTransactionData("Success".getBytes())
                        .withExecHash("execHash".getBytes())
                        .withManagedParties(Set.of(senderPublicKey))
                        .withSender(senderPublicKey)
                        .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
                        .build();

        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class))).thenReturn(response);

        String transactionHash = Base64.getEncoder().encodeToString("transactionHash".getBytes());

        Response result = jersey.target("transaction").path(transactionHash).request().get();

        assertThat(result.getStatus()).isEqualTo(200);

        com.quorum.tessera.api.ReceiveResponse resultResponse =
                result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);

        assertThat(resultResponse.getExecHash()).isEqualTo("execHash");
        assertThat(resultResponse.getPrivacyFlag()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION.getPrivacyFlag());
        assertThat(resultResponse.getAffectedContractTransactions()).isNullOrEmpty();
        assertThat(resultResponse.getPayload()).isEqualTo("Success".getBytes());
        assertThat(resultResponse.getManagedParties()).containsExactlyInAnyOrder(senderPublicKey.encodeToBase64());
        assertThat(resultResponse.getSenderKey()).isEqualTo(senderPublicKey.encodeToBase64());
        assertThat(resultResponse.getPrivacyGroupId()).isEqualTo(PublicKey.from("group".getBytes()).encodeToBase64());

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }

    @Test
    public void receive() {
        PublicKey sender = PublicKey.from("sender".getBytes());

        ReceiveResponse response = mock(ReceiveResponse.class);
        when(response.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(response.getUnencryptedTransactionData()).thenReturn("Success".getBytes());
        when(response.sender()).thenReturn(sender);

        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class))).thenReturn(response);

        String transactionHash = Base64.getEncoder().encodeToString("transactionHash".getBytes());

        Response result = jersey.target("transaction").path(transactionHash).request().get();

        assertThat(result.getStatus()).isEqualTo(200);

        com.quorum.tessera.api.ReceiveResponse resultResponse =
                result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);
        assertThat(resultResponse.getExecHash()).isNull();
        assertThat(resultResponse.getPrivacyFlag()).isEqualTo(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());

        assertThat(resultResponse.getExecHash()).isNull();
        assertThat(resultResponse.getAffectedContractTransactions()).isNull();
        assertThat(resultResponse.getPayload()).isEqualTo("Success".getBytes());
        assertThat(resultResponse.getSenderKey()).isEqualTo(sender.encodeToBase64());

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }

    @Test
    public void send() {

        final Base64.Encoder base64Encoder = Base64.getEncoder();

        final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));
        sendRequest.setTo(base64Key);

        final PublicKey sender =
                PublicKey.from(Base64.getDecoder().decode("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc="));
        when(transactionManager.defaultPublicKey()).thenReturn(sender);

        final com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        final MessageHash messageHash = mock(MessageHash.class);

        final byte[] txnData = "TxnData".getBytes();
        when(messageHash.getHashBytes()).thenReturn(txnData);

        when(sendResponse.getTransactionHash()).thenReturn(messageHash);
        when(sendResponse.getManagedParties()).thenReturn(Set.of(sender));

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        final Response result =
                jersey.target("send").request().post(Entity.entity(sendRequest, "application/vnd.tessera-2.1+json"));

        assertThat(result.getStatus()).isEqualTo(201);

        assertThat(result.getLocation().getPath()).isEqualTo("/transaction/" + base64Encoder.encodeToString(txnData));
        SendResponse resultSendResponse = result.readEntity(SendResponse.class);
        assertThat(resultSendResponse.getKey()).isEqualTo(Base64.getEncoder().encodeToString(txnData));
        assertThat(resultSendResponse.getManagedParties()).containsExactlyInAnyOrder(sender.encodeToBase64());

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
        assertThat(businessObject.getPrivacyGroupId()).isEmpty();
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

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        final Response result =
                jersey.target("send").request().post(Entity.entity(sendRequest, "application/vnd.tessera-2.1+json"));

        assertThat(result.getStatus()).isEqualTo(201);

        assertThat(result.getLocation().getPath()).isEqualTo("/transaction/" + base64Encoder.encodeToString(txnData));
        SendResponse resultSendResponse = result.readEntity(SendResponse.class);
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
    public void sendToPrivacyGroup() {

        final Base64.Encoder base64Encoder = Base64.getEncoder();

        final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));
        sendRequest.setPrivacyGroupId(base64Key);

        final PublicKey sender = mock(PublicKey.class);
        when(transactionManager.defaultPublicKey()).thenReturn(sender);

        final com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        final MessageHash messageHash = mock(MessageHash.class);

        final byte[] txnData = "TxnData".getBytes();
        when(messageHash.getHashBytes()).thenReturn(txnData);

        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        PrivacyGroup retrieved = mock(PrivacyGroup.class);
        PrivacyGroup.Id groupId = PrivacyGroup.Id.fromBase64String(base64Key);
        PublicKey member = PublicKey.from("member".getBytes());
        when(retrieved.getId()).thenReturn(groupId);
        when(retrieved.getMembers()).thenReturn(List.of(member));
        when(privacyGroupManager.retrievePrivacyGroup(groupId)).thenReturn(retrieved);

        final Response result =
                jersey.target("send").request().post(Entity.entity(sendRequest, "application/vnd.tessera-3.0+json"));

        assertThat(result.getStatus()).isEqualTo(201);

        assertThat(result.getLocation().getPath()).isEqualTo("/transaction/" + base64Encoder.encodeToString(txnData));
        SendResponse resultSendResponse = result.readEntity(SendResponse.class);
        assertThat(resultSendResponse.getKey()).isEqualTo(Base64.getEncoder().encodeToString(txnData));

        ArgumentCaptor<com.quorum.tessera.transaction.SendRequest> argumentCaptor =
                ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendRequest.class);

        verify(transactionManager).send(argumentCaptor.capture());
        verify(transactionManager).defaultPublicKey();
        verify(privacyGroupManager).retrievePrivacyGroup(groupId);

        com.quorum.tessera.transaction.SendRequest businessObject = argumentCaptor.getValue();

        assertThat(businessObject).isNotNull();
        assertThat(businessObject.getPayload()).isEqualTo(sendRequest.getPayload());
        assertThat(businessObject.getSender()).isEqualTo(sender);
        assertThat(businessObject.getRecipients()).hasSize(1);
        assertThat(businessObject.getRecipients().get(0)).isEqualTo(member);

        assertThat(businessObject.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(businessObject.getAffectedContractTransactions()).isEmpty();
        assertThat(businessObject.getExecHash()).isEmpty();

        assertThat(businessObject.getPrivacyGroupId()).isPresent().get().isEqualTo(groupId);
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

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        final Response result =
                jersey.target("send").request().post(Entity.entity(sendRequest, "application/vnd.tessera-2.1+json"));

        assertThat(result.getStatus()).isEqualTo(201);

        assertThat(result.getLocation().getPath()).isEqualTo("/transaction/" + base64Encoder.encodeToString(txnData));

        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void sendSignedTransactionEmptyRecipients() {

        final PublicKey sender = PublicKey.from("sender".getBytes());
        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        byte[] transactionHashData = "I Love Sparrows".getBytes();
        final String base64EncodedTransactionHAshData = Base64.getEncoder().encodeToString(transactionHashData);
        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn(transactionHashData);

        when(sendResponse.getTransactionHash()).thenReturn(transactionHash);
        when(sendResponse.getSender()).thenReturn(sender);

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class)))
                .thenReturn(sendResponse);

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setHash("SOMEDATA".getBytes());

        Response result =
                jersey.target("sendsignedtx")
                        .request()
                        .header("Content-Type", "application/vnd.tessera-2.1+json")
                        .header("Accept", "application/vnd.tessera-2.1+json")
                        .post(Entity.entity(sendSignedRequest, "application/vnd.tessera-2.1+json"));

        assertThat(result.getStatus()).isEqualTo(201);

        SendResponse resultResponse = result.readEntity(SendResponse.class);

        assertThat(resultResponse.getKey()).isEqualTo(base64EncodedTransactionHAshData);
        assertThat(resultResponse.getSenderKey()).isEqualTo(sender.encodeToBase64());

        assertThat(result.getLocation()).hasPath("/transaction/".concat(base64EncodedTransactionHAshData));

        ArgumentCaptor<com.quorum.tessera.transaction.SendSignedRequest> argumentCaptor =
                ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendSignedRequest.class);

        verify(transactionManager).sendSignedTransaction(argumentCaptor.capture());

        com.quorum.tessera.transaction.SendSignedRequest obj = argumentCaptor.getValue();

        assertThat(obj).isNotNull();
        assertThat(obj.getSignedData()).isEqualTo("SOMEDATA".getBytes());
        assertThat(obj.getRecipients()).hasSize(0);
        assertThat(obj.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(obj.getAffectedContractTransactions()).isEmpty();
        assertThat(obj.getExecHash()).isEmpty();
    }

    @Test
    public void sendSignedTransaction() {
        final PublicKey sender =
                PublicKey.from(Base64.getDecoder().decode("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc="));

        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        byte[] transactionHashData = "I Love Sparrows".getBytes();
        final String base64EncodedTransactionHAshData = Base64.getEncoder().encodeToString(transactionHashData);
        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn(transactionHashData);

        when(sendResponse.getTransactionHash()).thenReturn(transactionHash);
        when(sendResponse.getManagedParties()).thenReturn(Set.of(sender));
        when(sendResponse.getSender()).thenReturn(sender);

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class)))
                .thenReturn(sendResponse);

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setHash("SOMEDATA".getBytes());
        sendSignedRequest.setTo("recipient1", "recipient2");

        Response result =
                jersey.target("sendsignedtx")
                        .request()
                        .post(Entity.entity(sendSignedRequest, "application/vnd.tessera-2.1+json"));

        assertThat(result.getStatus()).isEqualTo(201);

        SendResponse resultResponse = result.readEntity(SendResponse.class);

        assertThat(resultResponse.getKey()).isEqualTo(base64EncodedTransactionHAshData);
        assertThat(resultResponse.getManagedParties()).containsExactlyInAnyOrder(sender.encodeToBase64());
        assertThat(resultResponse.getSenderKey()).isEqualTo(sender.encodeToBase64());

        assertThat(result.getLocation()).hasPath("/transaction/".concat(base64EncodedTransactionHAshData));

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
    public void sendSignedTransactionToPrivacyGroup() {

        final PublicKey sender =
                PublicKey.from(Base64.getDecoder().decode("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc="));
        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        byte[] transactionHashData = "I Love Sparrows".getBytes();
        final String base64EncodedTransactionHAshData = Base64.getEncoder().encodeToString(transactionHashData);
        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn(transactionHashData);

        when(sendResponse.getTransactionHash()).thenReturn(transactionHash);
        when(sendResponse.getManagedParties()).thenReturn(Set.of(sender));
        when(sendResponse.getSender()).thenReturn(sender);

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class)))
                .thenReturn(sendResponse);

        PrivacyGroup.Id groupId = PrivacyGroup.Id.fromBytes("groupId".getBytes());

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setHash("SOMEDATA".getBytes());
        sendSignedRequest.setPrivacyGroupId(groupId.getBase64());

        final PrivacyGroup pg = mock(PrivacyGroup.class);
        when(pg.getMembers()).thenReturn(List.of(PublicKey.from("r1".getBytes()), PublicKey.from("r2".getBytes())));
        when(pg.getId()).thenReturn(PrivacyGroup.Id.fromBytes("groupId".getBytes()));

        when(privacyGroupManager.retrievePrivacyGroup(groupId)).thenReturn(pg);

        Response result =
                jersey.target("sendsignedtx")
                        .request()
                        .post(Entity.entity(sendSignedRequest, "application/vnd.tessera-3.0+json"));

        assertThat(result.getStatus()).isEqualTo(201);

        SendResponse resultResponse = result.readEntity(SendResponse.class);

        assertThat(resultResponse.getKey()).isEqualTo(base64EncodedTransactionHAshData);

        assertThat(result.getLocation()).hasPath("/transaction/".concat(base64EncodedTransactionHAshData));

        ArgumentCaptor<com.quorum.tessera.transaction.SendSignedRequest> argumentCaptor =
                ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendSignedRequest.class);

        verify(privacyGroupManager).retrievePrivacyGroup(groupId);
        verify(transactionManager).sendSignedTransaction(argumentCaptor.capture());

        com.quorum.tessera.transaction.SendSignedRequest obj = argumentCaptor.getValue();

        assertThat(obj).isNotNull();
        assertThat(obj.getSignedData()).isEqualTo("SOMEDATA".getBytes());
        assertThat(obj.getRecipients()).hasSize(2);
        assertThat(obj.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(obj.getAffectedContractTransactions()).isEmpty();
        assertThat(obj.getExecHash()).isEmpty();
        assertThat(obj.getPrivacyGroupId()).isPresent().get().isEqualTo(groupId);
    }

    @Test
    public void sendSignedTransactionWithPrivacy() {
        final PublicKey sender =
                PublicKey.from(Base64.getDecoder().decode("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc="));

        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        byte[] transactionHashData = "I Love Sparrows".getBytes();
        final String base64EncodedTransactionHAshData = Base64.getEncoder().encodeToString(transactionHashData);
        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn(transactionHashData);

        when(sendResponse.getTransactionHash()).thenReturn(transactionHash);
        when(sendResponse.getSender()).thenReturn(sender);

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class)))
                .thenReturn(sendResponse);

        final String base64AffectedHash1 = Base64.getEncoder().encodeToString("aHash1".getBytes());
        final String base64AffectedHash2 = Base64.getEncoder().encodeToString("aHash2".getBytes());

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setHash("SOMEDATA".getBytes());
        sendSignedRequest.setTo("recipient1", "recipient2");
        sendSignedRequest.setPrivacyFlag(3);
        sendSignedRequest.setAffectedContractTransactions(base64AffectedHash1, base64AffectedHash2);
        sendSignedRequest.setExecHash("execHash");

        Response result =
                jersey.target("sendsignedtx")
                        .request()
                        .post(Entity.entity(sendSignedRequest, "application/vnd.tessera-2.1+json"));

        assertThat(result.getStatus()).isEqualTo(201);

        SendResponse resultResponse = result.readEntity(SendResponse.class);

        assertThat(resultResponse.getKey()).isEqualTo(base64EncodedTransactionHAshData);
        assertThat(resultResponse.getSenderKey()).isEqualTo(sender.encodeToBase64());

        assertThat(result.getLocation()).hasPath("/transaction/".concat(base64EncodedTransactionHAshData));

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
    public void deleteKey() {

        String encodedTxnHash = Base64.getEncoder().encodeToString("KEY".getBytes());
        List<MessageHash> results = new ArrayList<>();
        doAnswer((iom) -> results.add(iom.getArgument(0))).when(transactionManager).delete(any(MessageHash.class));

        Response response = jersey.target("transaction").path(encodedTxnHash).request().delete();

        assertThat(results).hasSize(1).extracting(MessageHash::getHashBytes).containsExactly("KEY".getBytes());

        assertThat(response.getStatus()).isEqualTo(204);

        verify(transactionManager).delete(any(MessageHash.class));
    }

    @Test
    public void isSenderDelegates() {
        when(transactionManager.isSender(any(MessageHash.class))).thenReturn(true);

        String senderKey = Base64.getEncoder().encodeToString("DUMMY_HASH".getBytes());

        Response response = jersey.target("transaction").path(senderKey).path("isSender").request().get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Boolean.class)).isEqualTo(true);
        verify(transactionManager).isSender(any(MessageHash.class));
    }

    @Test
    public void getParticipantsDelegates() {
        byte[] data = "DUMMY_HASH".getBytes();

        final String dummyPtmHash = Base64.getEncoder().encodeToString(data);

        PublicKey recipient = mock(PublicKey.class);
        when(recipient.encodeToBase64()).thenReturn("BASE64ENCODEKEY");

        when(transactionManager.getParticipants(any(MessageHash.class))).thenReturn(List.of(recipient));

        Response response = jersey.target("transaction").path(dummyPtmHash).path("participants").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("BASE64ENCODEKEY");
        verify(transactionManager).getParticipants(any(MessageHash.class));
    }

    @Test
    public void validationSendPayloadCannotBeNullOrEmpty() {

        Collection<Entity> nullAndEmpty =
                List.of(
                        Entity.entity(null, "application/vnd.tessera-2.1+json"),
                        Entity.entity(new byte[0], "application/vnd.tessera-2.1+json"));

        Map<String, Collection<Entity>> pathToEntityMapping = Map.of("sendsignedtx", nullAndEmpty);

        pathToEntityMapping.entrySet().stream()
                .forEach(
                        e ->
                                e.getValue()
                                        .forEach(
                                                entity -> {
                                                    Response response =
                                                            jersey.target(e.getKey())
                                                                    .request()
                                                                    .post(
                                                                            Entity.entity(
                                                                                    null,
                                                                                    "application/vnd.tessera-2.1+json"));
                                                    assertThat(response.getStatus()).isEqualTo(400);
                                                }));
    }

    @Test
    public void validationReceiveIsRawMustBeBoolean() {
        Response response = jersey.target("transaction").path("MYHASH").queryParam("isRaw", "bogus").request().get();
        assertThat(response.getStatus()).isEqualTo(400);
    }
}
