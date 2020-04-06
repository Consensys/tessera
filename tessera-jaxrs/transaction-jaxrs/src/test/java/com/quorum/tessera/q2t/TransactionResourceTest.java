package com.quorum.tessera.q2t;

import com.quorum.tessera.api.model.*;
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

        ReceiveRequest receiveRequest = new ReceiveRequest();

        Response result = transactionResource.receive(receiveRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        verify(transactionManager).receive(receiveRequest);
    }

    @Test
    public void receiveFromParams() {

        Response result = transactionResource.receive("", "");
        assertThat(result.getStatus()).isEqualTo(200);
        verify(transactionManager).receive(any(ReceiveRequest.class));
    }

    @Test
    public void receiveRaw() {

        byte[] encodedPayload = Base64.getEncoder().encode("Payload".getBytes());
        ReceiveResponse receiveResponse = mock(ReceiveResponse.class);
        when(receiveResponse.getPayload()).thenReturn(encodedPayload);

        when(transactionManager.receive(any(ReceiveRequest.class))).thenReturn(receiveResponse);

        Response result = transactionResource.receiveRaw("", "");
        assertThat(result.getStatus()).isEqualTo(200);
        verify(transactionManager).receive(any(ReceiveRequest.class));
    }

    @Test
    public void send() throws Exception {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(Base64.getEncoder().encode("PAYLOAD".getBytes()));

        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.send(any(SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.send(sendRequest);
        assertThat(result.getStatus()).isEqualTo(201);

        assertThat(result.getLocation().getPath()).isEqualTo("transaction/KEY");

        verify(transactionManager).send(any(SendRequest.class));
    }

    @Test
    public void sendSignedTransaction() throws UnsupportedEncodingException {
        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.sendSignedTransaction(any(SendSignedRequest.class))).thenReturn(sendResponse);
        Response result = transactionResource.sendSignedTransaction("someone", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("KEY");
        verify(transactionManager).sendSignedTransaction(any(SendSignedRequest.class));
    }

    @Test
    public void sendSignedTransactionEmptyRecipients() throws UnsupportedEncodingException {
        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.sendSignedTransaction(any(SendSignedRequest.class))).thenReturn(sendResponse);
        Response result = transactionResource.sendSignedTransaction("", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("KEY");
        verify(transactionManager).sendSignedTransaction(any(SendSignedRequest.class));
    }

    @Test
    public void sendRaw() throws UnsupportedEncodingException {

        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.send(any(SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", "someone", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("KEY");
        verify(transactionManager).send(any(SendRequest.class));
    }

    @Test
    public void sendRawEmptyRecipients() throws UnsupportedEncodingException {

        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.send(any(SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", "", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("KEY");
        verify(transactionManager).send(any(SendRequest.class));
    }

    @Test
    public void sendRawNullRecipient() throws UnsupportedEncodingException {

        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.send(any(SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", null, "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("KEY");
        verify(transactionManager).send(any(SendRequest.class));
    }

    @Test
    public void deleteKey() {

        List<DeleteRequest> results = new ArrayList<>();
        doAnswer((iom) -> results.add(iom.getArgument(0))).when(transactionManager).delete(any(DeleteRequest.class));

        Response response = transactionResource.deleteKey("KEY");

        assertThat(results).hasSize(1).extracting(DeleteRequest::getKey).containsExactly("KEY");

        assertThat(response.getStatus()).isEqualTo(204);

        verify(transactionManager).delete(any(DeleteRequest.class));
    }

    @Test
    public void delete() {

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey("KEY");

        Response response = transactionResource.delete(deleteRequest);

        assertThat(response.getStatus()).isEqualTo(200);

        verify(transactionManager).delete(deleteRequest);
    }
}
