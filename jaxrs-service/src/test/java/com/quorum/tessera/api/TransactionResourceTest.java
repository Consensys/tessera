package com.quorum.tessera.api;

import com.quorum.tessera.transaction.TransactionManagerImpl;
import com.quorum.tessera.api.model.*;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.transaction.PayloadEncoder;
import com.quorum.tessera.transaction.TransactionServiceImpl;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.util.Base64Decoder;
import com.quorum.tessera.util.exception.DecodingException;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {


    private Base64Decoder base64Decoder = Base64Decoder.create();

    private TransactionResource transactionResource;

    private PayloadEncoder payloadEncoder;
    
    private TransactionServiceImpl transactionService;
    
    @Before
    public void onSetup() {
        this.payloadEncoder = mock(PayloadEncoder.class);
        this.transactionService = mock(TransactionServiceImpl.class);
        transactionResource = new TransactionResource(new TransactionManagerImpl(base64Decoder,payloadEncoder,transactionService));
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder,transactionService);
    }

    @Test
    public void testSend() {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom("bXlwdWJsaWNrZXk=");
        sendRequest.setTo(new String[]{"cmVjaXBpZW50MQ=="});
        sendRequest.setPayload("Zm9v");

        when(transactionService.store(any(), any(), any())).thenReturn(new MessageHash("SOMEKEY".getBytes()));

        Response response = transactionResource.send(sendRequest);

        verify(transactionService, times(1)).store(any(), any(), any());
        assertThat(response).isNotNull();
        SendResponse sr = (SendResponse) response.getEntity();
        assertThat(sr.getKey()).isNotEmpty();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testSendRaw() {
        final byte[] payload = "Zm9v".getBytes();

        doReturn(new MessageHash("SOMEKEY".getBytes())).when(transactionService).store(any(), any(), eq(payload));

        String senderKey = "bXlwdWJsaWNrZXk=";
        final Response response = transactionResource.sendRaw(senderKey, "cmVjaXBpZW50MQ==", payload);

        verify(transactionService).store(any(Optional.class), any(byte[][].class), eq(payload));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void sendrawWithNoRecipients() {
        final byte[] payload = "Zm9v".getBytes();

        doReturn(new MessageHash("SOMEKEY".getBytes())).when(transactionService).store(any(), any(), eq(payload));

        String senderKey = "bXlwdWJsaWNrZXk=";
        final Response response = transactionResource.sendRaw(senderKey, null, payload);

        verify(transactionService).store(any(Optional.class), any(byte[][].class), eq(payload));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Ignore
    public void sendThrowsDecodingException() {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom("bXlwdWJsaWNrZXk=");
        sendRequest.setTo(new String[]{"cmVjaXBpZW50MQ=="});
        sendRequest.setPayload("Zm9v");

        when(transactionService.store(any(), any(), any())).thenThrow(new IllegalArgumentException());

        try {
            transactionResource.send(sendRequest);
            Assertions.failBecauseExceptionWasNotThrown(DecodingException.class);
        } catch (DecodingException ex) {
            Assertions.assertThat(ex).isNotNull();
        }
        verify(transactionService, times(1)).store(any(), any(), any());

    }

    @Test
    public void receiveWithValidParameters() {

        doReturn("SOME DATA".getBytes()).when(transactionService).receive(any(), any());

        Response response = transactionResource
                .receive("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=", "cmVjaXBpZW50MQ==");

//        verify(transactionService).receive(any(), any());
        assertThat(response).isNotNull();

        ReceiveResponse receiveResponse = (ReceiveResponse) response.getEntity();

        assertThat(receiveResponse.getPayload()).isEqualTo("U09NRSBEQVRB");
        verify(transactionService).receive(any(), any());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void receiveWithNoToField() {

        doReturn("SOME DATA".getBytes()).when(transactionService).receive(any(), any());

        Response response = transactionResource
                .receive("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=", "");

//        verify(transactionService).receive(any(), any());
        assertThat(response).isNotNull();

        ReceiveResponse receiveResponse = (ReceiveResponse) response.getEntity();

        assertThat(receiveResponse.getPayload()).isEqualTo("U09NRSBEQVRB");
        verify(transactionService).receive(any(), any());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @Deprecated
    public void receiveWithDeprecatedEndpoint() {

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        receiveRequest.setTo("cmVjaXBpZW50MQ==");

        doReturn("SOME DATA".getBytes()).when(transactionService).receive(any(), any());

        Response response = transactionResource.receive(receiveRequest);

//        verify(transactionService).receive(any(), any());
        assertThat(response).isNotNull();

        ReceiveResponse receiveResponse = (ReceiveResponse) response.getEntity();

        assertThat(receiveResponse.getPayload()).isEqualTo("U09NRSBEQVRB");
        verify(transactionService).receive(any(), any());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testReceiveRaw() {

        String key = "AFT757zkDmMksHdut9zeFXdd5wptBNlZtxrjlvuJkihf+rb6VH+go28Ih0nJ3wvCDei02sCcoN++Qbp5hULokQ==";

        String recipientKey = "cmVjaXBpZW50MQ==";

        when(transactionService.receive(any(), any())).thenReturn("SOMEKEY".getBytes());

        Response response = transactionResource.receiveRaw(key, recipientKey);

        verify(transactionService).receive(any(), any());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test(expected = DecodingException.class)
    public void testReceiveThrowDecodingException() {
//        when(transactionService.receive(any(), any())).thenReturn("SOME DATA".getBytes());

        Response response = transactionResource.receive("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=", "1");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);

    }

    @Test
    public void testDelete() {
       
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey(Base64.getEncoder().encodeToString("HELLOW".getBytes()));
        Response response = transactionResource.delete(deleteRequest);
        verify(transactionService, times(1)).delete(any());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testResendAll() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.ALL);
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey("mykey");

        Response response = transactionResource.resend(resendRequest);
        byte[] decodedKey = base64Decoder.decode(resendRequest.getPublicKey());

        verify(transactionService).resendAll(decodedKey);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testResendIndividual() {

        final Key sender = new Key(new byte[]{});
        final Nonce nonce = new Nonce(new byte[]{});

        final EncodedPayloadWithRecipients epwr = new EncodedPayloadWithRecipients(
            new EncodedPayload(sender, new byte[]{}, nonce, emptyList(), nonce),
            emptyList()
        );

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.INDIVIDUAL);
        resendRequest.setPublicKey("mypublickey");
        resendRequest.setKey(Base64.getEncoder().encodeToString("mykey".getBytes()));

        when(transactionService.fetchTransactionForRecipient(any(), any())).thenReturn(epwr);

        when(payloadEncoder.encode(epwr)).thenReturn("ENCODED".getBytes());
        
        Response response = transactionResource.resend(resendRequest);

        verify(transactionService).fetchTransactionForRecipient(any(), any());
        verify(payloadEncoder).encode(epwr);
        
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testPush() {
        when(transactionService.storePayload(any())).thenReturn(new MessageHash("somehash".getBytes()));
        Response response = transactionResource.push("SOMEDATA".getBytes());
        verify(transactionService).storePayload(any());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testDeleteKey() {


        String key = Base64.getEncoder().encodeToString("HELLOW".getBytes());
        
        transactionResource.deleteKey(key);
        verify(transactionService, times(1)).delete(any());

    }

}
