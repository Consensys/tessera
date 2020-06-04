package com.quorum.tessera.q2t;

import com.quorum.tessera.api.DeleteRequest;
import com.quorum.tessera.api.ReceiveRequest;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.ReceiveResponse;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {

    private TransactionResource transactionResource;

    private TransactionManager transactionManager;

    @Before
    public void onSetup() {

        transactionManager = mock(TransactionManager.class);
        transactionResource = new TransactionResource(transactionManager);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void receive() {
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);

        ReceiveResponse receiveResponse = mock(ReceiveResponse.class);
        when(transactionManager.receive(any())).thenReturn(receiveResponse);
        when(receiveResponse.getUnencryptedTransactionData()).thenReturn("Result".getBytes());

        Response result = transactionResource.receive(receiveRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isExactlyInstanceOf(com.quorum.tessera.api.ReceiveResponse.class);
        verify(transactionManager)
            .receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }

    @Test
    public void receiveWithRecipient() {
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);
        receiveRequest.setTo(Base64.getEncoder().encodeToString("Reno Raynes".getBytes()));

        ReceiveResponse receiveResponse = mock(ReceiveResponse.class);
        when(transactionManager.receive(any())).thenReturn(receiveResponse);
        when(receiveResponse.getUnencryptedTransactionData()).thenReturn("Result".getBytes());

        Response result = transactionResource.receive(receiveRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isExactlyInstanceOf(com.quorum.tessera.api.ReceiveResponse.class);
        verify(transactionManager)
            .receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }
    @Test
    public void receiveFromParams() {

        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class))).thenReturn(mock(ReceiveResponse.class));

        Response result = transactionResource.receive("", "");
        assertThat(result.getStatus()).isEqualTo(200);
        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }

    @Test
    public void receiveRaw() {

        byte[] encodedPayload = Base64.getEncoder().encode("Payload".getBytes());
        com.quorum.tessera.transaction.ReceiveResponse receiveResponse = mock(com.quorum.tessera.transaction.ReceiveResponse.class);
        when(receiveResponse.getUnencryptedTransactionData()).thenReturn(encodedPayload);
        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class))).thenReturn(receiveResponse);

        Response result = transactionResource.receiveRaw("", "");
        assertThat(result.getStatus()).isEqualTo(200);
        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));
    }

    @Test
    public void send() throws Exception {

        final Base64.Encoder base64Encoder = Base64.getEncoder();

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));

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

        assertThat(result.getLocation().getPath()).isEqualTo("transaction/"+ base64Encoder.encodeToString(txnData));

        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void sendForRecipient() throws Exception {

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
        assertThat(result.getStatus()).isEqualTo(201);

        assertThat(result.getLocation().getPath()).isEqualTo("transaction/"+ base64Encoder.encodeToString(txnData));

        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }


    @Test
    public void sendSignedTransaction() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse = mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

            //new SendResponse("KEY");
        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class))).thenReturn(sendResponse);
        Response result = transactionResource.sendSignedTransaction("someone", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString("KEY".getBytes()));
        verify(transactionManager).sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class));
    }

    @Test
    public void sendSignedTransactionEmptyRecipients() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse = mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class))).thenReturn(sendResponse);
        Response result = transactionResource.sendSignedTransaction("", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString("KEY".getBytes()));
        verify(transactionManager).sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class));
    }

    @Test
    public void sendSignedTransaction() throws Exception {

        SendResponse sendResponse = new SendResponse("Key");

        SendSignedRequest sendRequest = mock(SendSignedRequest.class);

        when(transactionManager.sendSignedTransaction(sendRequest)).thenReturn(sendResponse);

        Response result = transactionResource.sendSignedTransaction(sendRequest);

        assertThat(result.getStatus()).isEqualTo(201);
        assertThat(result.getEntity()).isSameAs(sendResponse);
        verify(transactionManager).sendSignedTransaction(any(SendSignedRequest.class));
    }

    @Test
    public void sendRaw() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse = mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", "someone", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString(txnData));
        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void sendRawEmptyRecipients() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse = mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));
        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", "", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString(txnData));
        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void sendRawNullRecipient() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse = mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);
        when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));
        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", null, "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo(Base64.getEncoder().encodeToString(txnData));
        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void deleteKey() {


        String encodedTxnHash = Base64.getEncoder().encodeToString("KEY".getBytes());
        List<MessageHash> results = new ArrayList<>();
        doAnswer((iom) -> results.add(iom.getArgument(0))).when(transactionManager).delete(any(MessageHash.class));

        Response response = transactionResource.deleteKey(encodedTxnHash);

        assertThat(results).hasSize(1)
            .extracting(MessageHash::getHashBytes)
            .containsExactly("KEY".getBytes());

        assertThat(response.getStatus()).isEqualTo(204);

        verify(transactionManager).delete(any(MessageHash.class));
    }

    @Test
    public void delete() {

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey("KEY");

        Response response = transactionResource.delete(deleteRequest);

        assertThat(response.getStatus()).isEqualTo(200);

        verify(transactionManager).delete(any(MessageHash.class));
    }

    @Test
    public void isSenderDelegates() {

        when(transactionManager.isSender(any(MessageHash.class))).thenReturn(true);

        Response response = transactionResource.isSender(Base64.getEncoder().encodeToString("DUMMY_HASH".getBytes()));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(true);
        verify(transactionManager).isSender(any(MessageHash.class));
    }

    @Test
    public void getParticipantsDelegates() {
        byte[] data = "DUMMY_HASH".getBytes();

        final String dummyPtmHash = Base64.getEncoder().encodeToString(data);

        PublicKey recipient = mock(PublicKey.class);
        when(recipient.encodeToBase64()).thenReturn("BASE64ENCODEKEY");

        when(transactionManager.getParticipants(any(MessageHash.class)))
            .thenReturn(List.of(recipient));

        Response response = transactionResource.getParticipants(dummyPtmHash);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo("BASE64ENCODEKEY");
        verify(transactionManager).getParticipants(any(MessageHash.class));
    }
}
