package com.quorum.tessera.api.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.grpc.model.*;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.transaction.PayloadEncoderImpl;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.util.Base64Decoder;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Base64;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionGrpcServiceTest {

    @Mock
    private Enclave enclave;

    @Mock
    private StreamObserver<SendResponse> sendResponseObserver;

    @Mock
    private StreamObserver<ReceiveResponse> receiveResponseObserver;

    @Mock
    private StreamObserver<DeleteRequest> deleteResponseObserver;

    @Mock
    private StreamObserver<PushRequest> pushResponseObserver;

    @Mock
    private StreamObserver<ResendResponse> resendResponseObserver;

    private TransactionGrpcService service;

    private Base64Decoder decoder = Base64Decoder.create();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new TransactionGrpcService(enclave, decoder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(
            enclave,
            sendResponseObserver,
            receiveResponseObserver,
            deleteResponseObserver,
            pushResponseObserver,
            resendResponseObserver);
    }

    @Test
    public void testSend() {
        SendRequest sendRequest = SendRequest.newBuilder()
            .setFrom("bXlwdWJsaWNrZXk=")
            .addTo("cmVjaXBpZW50MQ==")
            .setPayload("Zm9v")
            .build();

        when(enclave.store(any(), any(), any())).thenReturn(new MessageHash("SOMEKEY".getBytes()));

        service.send(sendRequest, sendResponseObserver);

        verify(enclave, times(1)).store(any(), any(), any());
        ArgumentCaptor<SendResponse> sendResponseCaptor = ArgumentCaptor.forClass(SendResponse.class);
        verify(sendResponseObserver).onNext(sendResponseCaptor.capture());
        SendResponse sendResponse = sendResponseCaptor.getValue();
        assertThat(sendResponse).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(sendResponse.getKey()))).isEqualTo("SOMEKEY");
        verify(sendResponseObserver).onCompleted();
    }

    @Test
    public void testSendWithEmptySender() {
        SendRequest sendRequest = SendRequest.newBuilder()
            .addTo("cmVjaXBpZW50MQ==")
            .setPayload("Zm9v")
            .build();

        when(enclave.store(any(), any(), any())).thenReturn(new MessageHash("SOMEKEY".getBytes()));

        service.send(sendRequest, sendResponseObserver);

        verify(enclave, times(1)).store(any(), any(), any());
        ArgumentCaptor<SendResponse> sendResponseCaptor = ArgumentCaptor.forClass(SendResponse.class);
        verify(sendResponseObserver).onNext(sendResponseCaptor.capture());
        SendResponse sendResponse = sendResponseCaptor.getValue();
        assertThat(sendResponse).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(sendResponse.getKey()))).isEqualTo("SOMEKEY");
        verify(sendResponseObserver).onCompleted();
    }

    @Test
    public void testReceive() {
        doReturn("SOME DATA".getBytes()).when(enclave).receive(any(), any());

        ReceiveRequest request = ReceiveRequest.newBuilder()
            .setTo("cmVjaXBpZW50MQ==")
            .setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=")
            .build();

        service.receive(request, receiveResponseObserver);

        verify(enclave).receive(any(), any());

        ArgumentCaptor<ReceiveResponse> receiveResponseCaptor = ArgumentCaptor.forClass(ReceiveResponse.class);
        verify(receiveResponseObserver).onNext(receiveResponseCaptor.capture());
        ReceiveResponse response = receiveResponseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(response.getPayload()))).isEqualTo("SOME DATA");
        verify(receiveResponseObserver).onCompleted();
    }

    @Test
    public void testReceiveWithNoToField() {
        doReturn("SOME DATA".getBytes()).when(enclave).receive(any(), any());

        ReceiveRequest request = ReceiveRequest.newBuilder()
            .setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=")
            .build();

        service.receive(request, receiveResponseObserver);

        verify(enclave).receive(any(), any());

        ArgumentCaptor<ReceiveResponse> receiveResponseCaptor = ArgumentCaptor.forClass(ReceiveResponse.class);
        verify(receiveResponseObserver).onNext(receiveResponseCaptor.capture());
        ReceiveResponse response = receiveResponseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(response.getPayload()))).isEqualTo("SOME DATA");
        verify(receiveResponseObserver).onCompleted();
    }

    @Test
    public void testDelete() {

        DeleteRequest request = DeleteRequest.newBuilder()
            .setKey(Base64.getEncoder().encodeToString("HELLOW".getBytes()))
            .build();

        service.delete(request, deleteResponseObserver);

        verify(enclave, times(1)).delete(any());

        ArgumentCaptor<DeleteRequest> responseCaptor = ArgumentCaptor.forClass(DeleteRequest.class);
        verify(deleteResponseObserver).onNext(responseCaptor.capture());
        DeleteRequest response = responseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(request);

        verify(deleteResponseObserver).onCompleted();
    }

    @Test
    public void testPush() {
        when(enclave.storePayload(any())).thenReturn(new MessageHash("somehash".getBytes()));

        PushRequest request = PushRequest.newBuilder().setData(ByteString.copyFrom("SOMEDATA".getBytes())).build();
        service.push(request, pushResponseObserver);

        verify(enclave).storePayload(any());

        ArgumentCaptor<PushRequest> responseCaptor = ArgumentCaptor.forClass(PushRequest.class);
        verify(pushResponseObserver).onNext(responseCaptor.capture());
        PushRequest response = responseCaptor.getValue();


        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(request);

        verify(pushResponseObserver).onCompleted();
    }

    @Test
    public void testResendAll() {

        ResendRequest request = ResendRequest.newBuilder()
            .setType(ResendRequestType.ALL)
            .setPublicKey("mypublickey")
            .setKey("mykey")
            .build();

        service.resend(request, resendResponseObserver);

        byte[] decodedKey = decoder.decode(request.getPublicKey());

        verify(enclave).resendAll(decodedKey);

        verify(resendResponseObserver).onNext(any());
        verify(resendResponseObserver).onCompleted();
    }

    @Test
    public void testResendIndividual() {

        final Key sender = new Key(new byte[]{});
        final Nonce nonce = new Nonce(new byte[]{});

        final EncodedPayloadWithRecipients epwr = new EncodedPayloadWithRecipients(
            new EncodedPayload(sender, new byte[]{}, nonce, emptyList(), nonce),
            emptyList()
        );

        ResendRequest request = ResendRequest.newBuilder()
            .setType(ResendRequestType.INDIVIDUAL)
            .setPublicKey("mypublickey")
            .setKey(Base64.getEncoder().encodeToString("mykey".getBytes()))
            .build();

        when(enclave.fetchTransactionForRecipient(any(), any())).thenReturn(epwr);

        service.resend(request, resendResponseObserver);

        verify(enclave).fetchTransactionForRecipient(any(), any());


        ArgumentCaptor<ResendResponse> responseCaptor = ArgumentCaptor.forClass(ResendResponse.class);
        verify(resendResponseObserver).onNext(responseCaptor.capture());
        ResendResponse response = responseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getData().toByteArray()).isEqualTo(new PayloadEncoderImpl().encode(epwr));

        verify(resendResponseObserver).onCompleted();
    }
}
