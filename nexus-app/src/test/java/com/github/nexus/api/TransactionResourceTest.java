package com.github.nexus.api;

import com.github.nexus.api.model.*;
import com.github.nexus.enclave.Enclave;
import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.util.Base64Decoder;
import com.github.nexus.util.exception.DecodingException;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Base64;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {

    private Enclave enclave;

    private Base64Decoder base64Decoder = Base64Decoder.create();

    private TransactionResource transactionResource;

    @Before
    public void onSetup() {
        this.enclave = mock(Enclave.class);
        transactionResource = new TransactionResource(enclave, base64Decoder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave);
    }

    @Test
    public void testSend() {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom("bXlwdWJsaWNrZXk=");
        sendRequest.setTo(new String[]{"cmVjaXBpZW50MQ=="});
        sendRequest.setPayload("Zm9v");

        when(enclave.store(any(), any(), any())).thenReturn(new MessageHash("SOMEKEY".getBytes()));

        Response response = transactionResource.send(sendRequest);

        verify(enclave, times(1)).store(any(), any(), any());
        assertThat(response).isNotNull();
        SendResponse sr = (SendResponse) response.getEntity();
        assertThat(sr.getKey()).isNotEmpty();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testSendRaw() {
        final HttpHeaders headers = mock(HttpHeaders.class);
        final byte[] payload = "Zm9v".getBytes();

        doReturn("bXlwdWJsaWNrZXk=").when(headers).getHeaderString("c11n-from");
        doReturn(singletonList("cmVjaXBpZW50MQ==")).when(headers).getRequestHeader("c11n-to");

        doReturn(new MessageHash("SOMEKEY".getBytes())).when(enclave).store(any(), any(), eq(payload));

        final Response response = transactionResource.sendRaw(headers, payload);

        verify(enclave).store(any(byte[].class), any(byte[][].class), eq(payload));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }


    @Ignore
    public void sendThrowsDecodingException() {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom("bXlwdWJsaWNrZXk=");
        sendRequest.setTo(new String[]{"cmVjaXBpZW50MQ=="});
        sendRequest.setPayload("Zm9v");

        when(enclave.store(any(), any(), any())).thenThrow(new IllegalArgumentException());

        try {
            transactionResource.send(sendRequest);
            Assertions.failBecauseExceptionWasNotThrown(DecodingException.class);
        } catch (DecodingException ex) {
            assertThat(ex).isNotNull();
        }
        verify(enclave, times(1)).store(any(), any(), any());

    }

    @Test
    public void testReceive() {

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        receiveRequest.setTo("cmVjaXBpZW50MQ==");

        when(enclave.receive(any(), any())).thenReturn("SOME DATA".getBytes());

        Response response = transactionResource.receive(receiveRequest);

//        verify(transactionService, times(1)).receive(any(), any());
        assertThat(response).isNotNull();

        ReceiveResponse receiveResponse = (ReceiveResponse) response.getEntity();

        assertThat(receiveResponse.getPayload()).isEqualTo("U09NRSBEQVRB");
        verify(enclave).receive(any(), any());
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testReceiveRaw() {
        HttpHeaders headers = mock(HttpHeaders.class);

        when(headers.getHeaderString("c11n-key"))
            .thenReturn("AFT757zkDmMksHdut9zeFXdd5wptBNlZtxrjlvuJkihf+rb6VH+go28Ih0nJ3wvCDei02sCcoN++Qbp5hULokQ==");

        when(headers.getHeaderString("c11n-to"))
            .thenReturn("cmVjaXBpZW50MQ==");

        when(enclave.receive(any(), any())).thenReturn("SOMEKEY".getBytes());

        Response response = transactionResource.receiveRaw(headers);

        verify(enclave).receive(any(), any());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test(expected = DecodingException.class)
    public void testReceiveThrowDecodingException() {
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        receiveRequest.setTo("1");

//        when(transactionService.receive(any(), any())).thenReturn("SOME DATA".getBytes());

        Response response = transactionResource.receive(receiveRequest);

        assertThat(response).isNotNull();
        ReceiveResponse receiveResponse = (ReceiveResponse) response.getEntity();
        assertThat(response.getStatus()).isEqualTo(400);

    }

    @Test
    public void testDelete() {
        when(enclave.delete(any())).thenReturn(true);
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey(Base64.getEncoder().encodeToString("HELLOW".getBytes()));
        Response response = transactionResource.delete(deleteRequest);
        verify(enclave, times(1)).delete(any());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testResendAllLowercase() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.ALL);
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey("mykey");

        Response response = transactionResource.resend(resendRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testResendIndividualLowercase() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.INDIVIDUAL);
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey("cmVjaXBpZW50MQ==");

        Response response = transactionResource.resend(resendRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testResendAll() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.ALL);
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey("mykey");

        Response response = transactionResource.resend(resendRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testResendIndividual() {

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.INDIVIDUAL);
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey(Base64.getEncoder().encodeToString("mykey".getBytes()));

        Response response = transactionResource.resend(resendRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testPush() {
        when(enclave.storePayload(any())).thenReturn(new MessageHash("somehash".getBytes()));
        Response response = transactionResource.push("SOMEDATA".getBytes());
        verify(enclave).storePayload(any());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

}
