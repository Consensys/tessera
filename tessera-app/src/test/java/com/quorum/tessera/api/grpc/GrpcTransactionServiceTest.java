package com.quorum.tessera.api.grpc;

import com.quorum.tessera.api.grpc.model.ReceiveRequest;
import com.quorum.tessera.api.grpc.model.ReceiveResponse;
import com.quorum.tessera.api.grpc.model.SendRequest;
import com.quorum.tessera.api.grpc.model.SendResponse;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.util.Base64Decoder;
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

public class GrpcTransactionServiceTest {

    @Mock
    private Enclave enclave;

    @Mock
    private StreamObserver<SendResponse> sendResponseObserver;

    @Mock
    private StreamObserver<ReceiveResponse> receiveResponseObserver;

    private GrpcTransactionService service;

    private Base64Decoder decoder = Base64Decoder.create();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new GrpcTransactionService(enclave, decoder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave, sendResponseObserver, receiveResponseObserver);
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
}
