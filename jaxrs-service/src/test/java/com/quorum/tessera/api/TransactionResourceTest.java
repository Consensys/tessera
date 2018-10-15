package com.quorum.tessera.api;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import java.util.Base64;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import com.quorum.tessera.transaction.TransactionManager;

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
    public void deleteKey() {
        Response result = transactionResource.deleteKey("somneKey");
        assertThat(result.getStatus()).isEqualTo(204);

        verify(transactionManager).delete(any(DeleteRequest.class));
    }

    @Test
    public void delete() {
        DeleteRequest deleteRequest = new DeleteRequest();

        Response result = transactionResource.delete(deleteRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        verify(transactionManager).delete(any(DeleteRequest.class));

    }

    @Test
    public void push() {
        byte[] someData = "SomeData".getBytes();
        Response result = transactionResource.push(someData);
        assertThat(result.getStatus()).isEqualTo(201);
        assertThat(result.hasEntity()).isTrue();
        verify(transactionManager).storePayload(someData);
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
        ReceiveResponse receiveResponse = new ReceiveResponse(encodedPayload);

        when(transactionManager.receive(any(ReceiveRequest.class))).thenReturn(receiveResponse);

        Response result = transactionResource.receiveRaw("", "");
        assertThat(result.getStatus()).isEqualTo(200);
        verify(transactionManager).receive(any(ReceiveRequest.class));
    }

    @Test
    public void resend() {

        ResendRequest resendRequest = mock(ResendRequest.class);
        ResendResponse resendResponse = new ResendResponse("SUCCESS".getBytes());

        when(transactionManager.resend(resendRequest)).thenReturn(resendResponse);

        Response result = transactionResource.resend(resendRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("SUCCESS".getBytes());
        verify(transactionManager).resend(resendRequest);

    }

    @Test
    public void send() {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(Base64.getEncoder().encode("PAYLOAD".getBytes()));
        
        Response result = transactionResource.send(sendRequest);
        assertThat(result.getStatus()).isEqualTo(200);

        verify(transactionManager).send(any(SendRequest.class));

    }

    @Test
    public void sendRaw() {

        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.send(any(SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", "someone", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("KEY");
        verify(transactionManager).send(any(SendRequest.class));

    }
    @Test
    public void sendRawEmptyRecipients() {

        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.send(any(SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", "", "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("KEY");
        verify(transactionManager).send(any(SendRequest.class));

    }
    @Test
    public void sendRawNullRecipient() {

        SendResponse sendResponse = new SendResponse("KEY");
        when(transactionManager.send(any(SendRequest.class))).thenReturn(sendResponse);

        Response result = transactionResource.sendRaw("", null, "".getBytes());
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("KEY");
        verify(transactionManager).send(any(SendRequest.class));

    }
}
