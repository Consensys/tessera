package com.github.nexus.api;

import com.github.nexus.util.Base64Decoder;
import com.github.nexus.api.exception.DecodingException;
import com.github.nexus.api.model.*;
import com.github.nexus.enclave.Enclave;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {

    @Mock
    private Enclave enclave;

    private TransactionResource transactionResource;

    private Base64Decoder base64Decoder = Base64Decoder.create();
    
    @Before
    public void onSetup() {
        MockitoAnnotations.initMocks(this);
        transactionResource = new TransactionResource(enclave,base64Decoder);
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

        when(enclave.store(any(), any(), any())).thenReturn("SOMEKEY".getBytes());

        Response response = transactionResource.send(sendRequest);

        verify(enclave, times(1)).store(any(), any(), any());
        assertThat(response).isNotNull();
        SendResponse sr = (SendResponse) response.getEntity();
        assertThat(sr.getKey()).isNotEmpty();
        assertThat(response.getStatus()).isEqualTo(201);
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
    public void testSendRaw() throws Exception {

        HttpHeaders headers = mock(HttpHeaders.class);

        when(headers.getRequestHeader("hFrom"))
                .thenReturn(Stream.of("c2VuZGVy")
                        .collect(Collectors.toList()));

        when(headers.getRequestHeader("hTo"))
                .thenReturn(Stream.of("cmVjaXBpZW50MQ==")
                        .collect(Collectors.toList()));

        Response response = transactionResource.sendRaw(headers, new ByteArrayInputStream("Zm9v".getBytes()));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);

    }

    @Test
    public void testReceive() {

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        receiveRequest.setTo("cmVjaXBpZW50MQ==");

//        when(enclave.receive(any(), any())).thenReturn("SOME DATA".getBytes());

        Response response = transactionResource.receive(receiveRequest);

//        verify(transactionService, times(1)).receive(any(), any());
        assertThat(response).isNotNull();

        ReceiveResponse receiveResponse = (ReceiveResponse) response.getEntity();

        assertThat(receiveResponse.getPayload()).isEqualTo("UmV0cmlldmVkIHBheWxvYWQ=");

        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test(expected = DecodingException.class)
    public void testReceiveThrowDecodingException(){
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
    public void testReceiveRaw() {

        HttpHeaders headers = mock(HttpHeaders.class);

        when(headers.getRequestHeader("hKey"))
                .thenReturn(Stream.of("FOO")
                        .collect(Collectors.toList()));

        when(headers.getRequestHeader("hTo"))
                .thenReturn(Stream.of("BAR")
                        .collect(Collectors.toList()));

        Response response = transactionResource.receiveRaw(headers);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testDelete() {

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey(Base64.getEncoder().encodeToString("HELLOW".getBytes()));
        Response response = transactionResource.delete(deleteRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testResendAllLowercase() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.ALL.name().toLowerCase());
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey("mykey");

        Response response = transactionResource.resend(resendRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testResendIndividualLowercase() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.INDIVIDUAL.name().toLowerCase());
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey("cmVjaXBpZW50MQ==");

        Response response = transactionResource.resend(resendRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testResendAll() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.ALL.name());
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey("mykey");

        Response response = transactionResource.resend(resendRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testResendIndividual() {

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.INDIVIDUAL.name());
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey(Base64.getEncoder().encodeToString("mykey".getBytes()));

        Response response = transactionResource.resend(resendRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testPush() throws IOException {

        Response response = transactionResource.push(new ByteArrayInputStream("SOMEDATA".getBytes()));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testReadInputStreamJustForCoverage() throws IOException {

        final String data = "I LOVE SPARROWS!!";

        InputStream inputStream = spy(new ByteArrayInputStream(data.getBytes()));

        String result = TransactionResource.readInputStream(inputStream);

        assertThat(result).isEqualTo(data);
        verify(inputStream).close();

    }

    @Test
    public void testReadInputStreamJustForCoverageThrowsIO() throws IOException {

        InputStream inputStream = mock(InputStream.class);

        try {
            TransactionResource.readInputStream(inputStream);
            fail();
        } catch (UncheckedIOException ex) {
            assertThat(ex).isNotNull();
        }
        verify(inputStream).close();

    }

}
