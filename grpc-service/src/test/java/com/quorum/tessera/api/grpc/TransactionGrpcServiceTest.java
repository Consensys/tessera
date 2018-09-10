package com.quorum.tessera.api.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.EnclaveDelegate;
import com.quorum.tessera.api.grpc.model.*;
import io.grpc.stub.StreamObserver;
import java.util.Base64;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionGrpcServiceTest {

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

    @Mock
    private EnclaveDelegate enclaveDelegate;

    private TransactionGrpcService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new TransactionGrpcService(enclaveDelegate);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(
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
                .setPayload("Zm9v").build();
        
        
        
        com.quorum.tessera.api.model.SendResponse r = new com.quorum.tessera.api.model.SendResponse("KEY");
        when(enclaveDelegate.send(any())).thenReturn(r);

        service.send(sendRequest,sendResponseObserver);

        verify(enclaveDelegate).send(any());

        verify(sendResponseObserver).onNext(any());

        verify(sendResponseObserver).onCompleted();
    }

    @Test
    public void testSendWithEmptySender() {
        SendRequest sendRequest = SendRequest.newBuilder()
                .addTo("cmVjaXBpZW50MQ==")
                .setPayload("Zm9v")
                .build();

        com.quorum.tessera.api.model.SendResponse r = new com.quorum.tessera.api.model.SendResponse("KEY");
        when(enclaveDelegate.send(any())).thenReturn(r);

        service.send(sendRequest, sendResponseObserver);


        verify(enclaveDelegate).send(any());

        verify(sendResponseObserver).onNext(any());

        verify(sendResponseObserver).onCompleted();
    }

    @Test
    public void testReceive() {
        
        when(enclaveDelegate.receiveAndEncode(any())).thenReturn("SOME DATA");

        ReceiveRequest request = ReceiveRequest.newBuilder()
                .setTo("cmVjaXBpZW50MQ==")
                .setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=")
                .build();

        service.receive(request, receiveResponseObserver);

        verify(enclaveDelegate).receiveAndEncode(any());

        ArgumentCaptor<ReceiveResponse> receiveResponseCaptor = ArgumentCaptor.forClass(ReceiveResponse.class);
        verify(receiveResponseObserver).onNext(receiveResponseCaptor.capture());
        ReceiveResponse response = receiveResponseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getPayload()).isEqualTo("SOME DATA");
        verify(receiveResponseObserver).onCompleted();
    }

    @Test
    public void testReceiveWithNoToField() {

        ReceiveRequest request = ReceiveRequest.newBuilder()
                .setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=")
                .build();
        
        when(enclaveDelegate.receiveAndEncode(any())).thenReturn("ENCODEDPAYLOAD");
        
        service.receive(request, receiveResponseObserver);

        verify(enclaveDelegate).receiveAndEncode(any());

        ArgumentCaptor<ReceiveResponse> receiveResponseCaptor = ArgumentCaptor.forClass(ReceiveResponse.class);
        verify(receiveResponseObserver).onNext(receiveResponseCaptor.capture());
        ReceiveResponse response = receiveResponseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getPayload()).isEqualTo("ENCODEDPAYLOAD");
        verify(receiveResponseObserver).onCompleted();
    }

    @Test
    public void testDelete() {

        DeleteRequest request = DeleteRequest.newBuilder()
                .setKey(Base64.getEncoder().encodeToString("HELLOW".getBytes()))
                .build();

        service.delete(request, deleteResponseObserver);

        verify(enclaveDelegate, times(1)).delete(any());

        ArgumentCaptor<DeleteRequest> responseCaptor = ArgumentCaptor.forClass(DeleteRequest.class);
        verify(deleteResponseObserver).onNext(responseCaptor.capture());
        DeleteRequest response = responseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(request);

        verify(deleteResponseObserver).onCompleted();
    }

    @Test
    public void testPush() {

        PushRequest request = PushRequest.newBuilder().setData(ByteString.copyFrom("SOMEDATA".getBytes())).build();
        service.push(request, pushResponseObserver);

        verify(enclaveDelegate).storePayload(any());

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

        when(enclaveDelegate.resendAndEncode(any())).thenReturn(Optional.empty());
        
        service.resend(request, resendResponseObserver);

        verify(enclaveDelegate).resendAndEncode(any());

        verify(resendResponseObserver).onNext(any());
        verify(resendResponseObserver).onCompleted();
    }
//
//    @Test
//    public void testResendIndividual() {
//
//        final Key sender = new Key(new byte[]{});
//        final Nonce nonce = new Nonce(new byte[]{});
//
//        final EncodedPayloadWithRecipients epwr = new EncodedPayloadWithRecipients(
//                new EncodedPayload(sender, new byte[]{}, nonce, emptyList(), nonce),
//                emptyList()
//        );
//
//        ResendRequest request = ResendRequest.newBuilder()
//                .setType(ResendRequestType.INDIVIDUAL)
//                .setPublicKey("mypublickey")
//                .setKey(Base64.getEncoder().encodeToString("mykey".getBytes()))
//                .build();
//
//        when(enclave.fetchTransactionForRecipient(any(), any())).thenReturn(epwr);
//
//        service.resend(request, resendResponseObserver);
//
//        verify(enclave).fetchTransactionForRecipient(any(), any());
//
//        ArgumentCaptor<ResendResponse> responseCaptor = ArgumentCaptor.forClass(ResendResponse.class);
//        verify(resendResponseObserver).onNext(responseCaptor.capture());
//        ResendResponse response = responseCaptor.getValue();
//
//        assertThat(response).isNotNull();
//        assertThat(response.getData().toByteArray()).isEqualTo(new PayloadEncoderImpl().encode(epwr));
//
//        verify(resendResponseObserver).onCompleted();
//    }
}
