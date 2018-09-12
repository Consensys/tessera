package com.quorum.tessera.api.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.enclave.EnclaveMediator;
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
    private EnclaveMediator enclaveMediator;

    private TransactionGrpcService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new TransactionGrpcService(enclaveMediator);
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
        when(enclaveMediator.send(any())).thenReturn(r);

        service.send(sendRequest, sendResponseObserver);

        verify(enclaveMediator).send(any());

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
        when(enclaveMediator.send(any())).thenReturn(r);

        service.send(sendRequest, sendResponseObserver);

        verify(enclaveMediator).send(any());

        verify(sendResponseObserver).onNext(any());

        verify(sendResponseObserver).onCompleted();
    }

    @Test
    public void testReceive() {

        when(enclaveMediator.receiveAndEncode(any())).thenReturn("SOME DATA");

        ReceiveRequest request = ReceiveRequest.newBuilder()
                .setTo("cmVjaXBpZW50MQ==")
                .setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=")
                .build();

        service.receive(request, receiveResponseObserver);

        verify(enclaveMediator).receiveAndEncode(any());

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


        service.receive(request, receiveResponseObserver);
        

        verify(receiveResponseObserver).onError(any());
    }

    @Test
    public void testDelete() {

        DeleteRequest request = DeleteRequest.newBuilder()
                .setKey(Base64.getEncoder().encodeToString("HELLOW".getBytes()))
                .build();

        service.delete(request, deleteResponseObserver);

        verify(enclaveMediator, times(1)).delete(any());

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

        verify(enclaveMediator).storePayload(any());

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

        when(enclaveMediator.resendAndEncode(any())).thenReturn(Optional.empty());

        service.resend(request, resendResponseObserver);

        verify(enclaveMediator).resendAndEncode(any());

        verify(resendResponseObserver).onNext(any());
        verify(resendResponseObserver).onCompleted();
    }


    @Test
    public void invalidSendRequest() {

        SendRequest sendRequest = SendRequest.newBuilder()
                .setFrom("bXlwdWJsaWNrZXk=")
                .addTo("cmVjaXBpZW50MQ==").build();

        service.send(sendRequest, sendResponseObserver);

        verify(sendResponseObserver).onError(any());

    }

    @Test
    public void invalidReceiveRequest() {

        ReceiveRequest request = ReceiveRequest.newBuilder()
                .build();

        service.receive(request, receiveResponseObserver);

        verify(receiveResponseObserver).onError(any());

    }

    @Test
    public void invalidDelete() {

        DeleteRequest request = DeleteRequest.newBuilder()
                .build();

        service.delete(request, deleteResponseObserver);

        verify(deleteResponseObserver).onError(any());

    }



}
