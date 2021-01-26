package com.quorum.tessera.q2t;

import com.quorum.tessera.api.BesuReceiveResponse;
import com.quorum.tessera.api.ReceiveRequest;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupId;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class BesuTransactionResourceTest {

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

        transactionManager = mock(TransactionManager.class);
        privacyGroupManager = mock(PrivacyGroupManager.class);

        BesuTransactionResource besuTransactionResource =
                new BesuTransactionResource(transactionManager, privacyGroupManager);

        jersey =
                new JerseyTest() {
                    @Override
                    protected Application configure() {
                        forceSet(TestProperties.CONTAINER_PORT, "0");
                        enable(TestProperties.LOG_TRAFFIC);
                        enable(TestProperties.DUMP_ENTITY);
                        return new ResourceConfig().register(besuTransactionResource);
                    }
                };

        jersey.setUp();
    }

    @After
    public void onTearDown() throws Exception {
        verifyNoMoreInteractions(transactionManager);
        verifyNoMoreInteractions(privacyGroupManager);
        jersey.tearDown();
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

        final byte[] recipientKeyBytes = Base64.getDecoder().decode(base64Key);

        final com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        final MessageHash messageHash = mock(MessageHash.class);

        final byte[] txnData = "TxnData".getBytes();
        when(messageHash.getHashBytes()).thenReturn(txnData);

        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        PrivacyGroup legacy = mock(PrivacyGroup.class);
        when(legacy.getPrivacyGroupId()).thenReturn(PrivacyGroupId.from("group".getBytes()));
        when(privacyGroupManager.createLegacyPrivacyGroup(eq(sender), eq(List.of(PublicKey.from(recipientKeyBytes)))))
                .thenReturn(legacy);

        final Response result =
                jersey.target("send").request().post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(200);

        assertThat(result.getLocation().getPath()).isEqualTo("/transaction/" + base64Encoder.encodeToString(txnData));
        SendResponse resultSendResponse = result.readEntity(SendResponse.class);
        assertThat(resultSendResponse.getKey()).isEqualTo(Base64.getEncoder().encodeToString(txnData));

        ArgumentCaptor<com.quorum.tessera.transaction.SendRequest> argumentCaptor =
                ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendRequest.class);

        verify(transactionManager).send(argumentCaptor.capture());
        verify(transactionManager).defaultPublicKey();
        verify(privacyGroupManager)
                .createLegacyPrivacyGroup(eq(sender), eq(List.of(PublicKey.from(recipientKeyBytes))));

        com.quorum.tessera.transaction.SendRequest businessObject = argumentCaptor.getValue();

        assertThat(businessObject).isNotNull();
        assertThat(businessObject.getPayload()).isEqualTo(sendRequest.getPayload());
        assertThat(businessObject.getSender()).isEqualTo(sender);
        assertThat(businessObject.getRecipients()).hasSize(1);
        assertThat(businessObject.getRecipients().get(0).encodeToBase64()).isEqualTo(base64Key);

        assertThat(businessObject.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(businessObject.getAffectedContractTransactions()).isEmpty();
        assertThat(businessObject.getExecHash()).isEmpty();

        assertThat(businessObject.getPrivacyGroupId())
                .isPresent()
                .get()
                .isEqualTo(PrivacyGroupId.from("group".getBytes()));
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
        PrivacyGroupId groupId = PrivacyGroupId.from(Base64.getDecoder().decode(base64Key));
        PublicKey member = PublicKey.from("member".getBytes());
        when(retrieved.getPrivacyGroupId()).thenReturn(groupId);
        when(retrieved.getMembers()).thenReturn(List.of(member));
        when(privacyGroupManager.retrievePrivacyGroup(groupId)).thenReturn(retrieved);

        final Response result =
                jersey.target("send").request().post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(200);

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
    public void receive() {
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);

        String recipient = Base64.getEncoder().encodeToString("Bobby Sixkiller".getBytes());

        receiveRequest.setTo(recipient);

        ReceiveResponse receiveResponse = mock(ReceiveResponse.class);

        when(receiveResponse.getAffectedTransactions()).thenReturn(Set.of());
        when(receiveResponse.getUnencryptedTransactionData()).thenReturn("Result".getBytes());
        when(receiveResponse.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(receiveResponse.sender()).thenReturn(PublicKey.from("sender".getBytes()));
        when(receiveResponse.getPrivacyGroupId()).thenReturn(Optional.of(PrivacyGroupId.from("group".getBytes())));

        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class)))
                .thenReturn(receiveResponse);

        BesuTransactionResource resource = new BesuTransactionResource(transactionManager, privacyGroupManager);

        final Response result = resource.receive(receiveRequest);

        assertThat(result.getStatus()).isEqualTo(200);

        BesuReceiveResponse resultResponse = (BesuReceiveResponse) result.getEntity();

        assertThat(resultResponse.getPayload()).isEqualTo("Result".getBytes());
        assertThat(resultResponse.getSenderKey()).isEqualTo(PublicKey.from("sender".getBytes()).encodeToBase64());
        assertThat(resultResponse.getPrivacyGroupId()).isEqualTo(PublicKey.from("group".getBytes()).encodeToBase64());

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }

    @Test
    public void receiveWithRecipient() {
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);
        receiveRequest.setTo(Base64.getEncoder().encodeToString("Reno Raynes".getBytes()));

        ReceiveResponse receiveResponse = mock(ReceiveResponse.class);
        when(receiveResponse.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(transactionManager.receive(any())).thenReturn(receiveResponse);
        when(receiveResponse.getUnencryptedTransactionData()).thenReturn("Result".getBytes());
        when(receiveResponse.sender()).thenReturn(PublicKey.from("sender".getBytes()));
        when(receiveResponse.getPrivacyGroupId()).thenReturn(Optional.of(PrivacyGroupId.from("group".getBytes())));

        BesuTransactionResource resource = new BesuTransactionResource(transactionManager, privacyGroupManager);
        final Response result = resource.receive(receiveRequest);

        assertThat(result.getStatus()).isEqualTo(200);

        BesuReceiveResponse resultResponse = (BesuReceiveResponse) result.getEntity();

        assertThat(resultResponse.getPayload()).isEqualTo("Result".getBytes());
        assertThat(resultResponse.getSenderKey()).isEqualTo(PublicKey.from("sender".getBytes()).encodeToBase64());
        assertThat(resultResponse.getPrivacyGroupId()).isEqualTo(PublicKey.from("group".getBytes()).encodeToBase64());

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }
}
