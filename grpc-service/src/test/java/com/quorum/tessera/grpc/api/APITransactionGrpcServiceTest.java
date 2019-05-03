package com.quorum.tessera.grpc.api;

import com.google.protobuf.ByteString;
import com.quorum.tessera.transaction.TransactionManagerImpl;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class APITransactionGrpcServiceTest {

    @Mock
    private StreamObserver<SendResponse> sendResponseObserver;

    @Mock
    private StreamObserver<ReceiveResponse> receiveResponseObserver;
    
    @Mock
    private TransactionManagerImpl enclaveMediator;

    private APITransactionGrpcService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new APITransactionGrpcService(enclaveMediator);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(sendResponseObserver, receiveResponseObserver);
    }

    @Test
    public void testSend() {

        SendRequest sendRequest = SendRequest.newBuilder()
                .setFrom("bXlwdWJsaWNrZXk=")
                .addTo("cmVjaXBpZW50MQ==")
                .setPayload(ByteString.copyFromUtf8("Zm9v")).build();

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
                .setPayload(ByteString.copyFromUtf8("Zm9v"))
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

        com.quorum.tessera.api.model.ReceiveResponse r = new com.quorum.tessera.api.model.ReceiveResponse("SOME DATA".getBytes());

        when(enclaveMediator.receive(any())).thenReturn(r);

        ReceiveRequest request = ReceiveRequest.newBuilder()
                .setTo("cmVjaXBpZW50MQ==")
                .setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=")
                .build();

        service.receive(request, receiveResponseObserver);

        verify(enclaveMediator).receive(any());

        ArgumentCaptor<ReceiveResponse> receiveResponseCaptor = ArgumentCaptor.forClass(ReceiveResponse.class);
        verify(receiveResponseObserver).onNext(receiveResponseCaptor.capture());
        ReceiveResponse response = receiveResponseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getPayload().toStringUtf8()).isEqualTo("SOME DATA");
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

}
