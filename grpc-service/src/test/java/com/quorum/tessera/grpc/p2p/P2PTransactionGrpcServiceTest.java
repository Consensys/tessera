package com.quorum.tessera.grpc.p2p;

import com.google.protobuf.ByteString;
import com.quorum.tessera.transaction.TransactionManagerImpl;
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

public class P2PTransactionGrpcServiceTest {

    @Mock
    private StreamObserver<DeleteRequest> deleteResponseObserver;

    @Mock
    private StreamObserver<PushRequest> pushResponseObserver;

    @Mock
    private StreamObserver<ResendResponse> resendResponseObserver;

    @Mock
    private TransactionManagerImpl enclaveMediator;

    private P2PTransactionGrpcService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new P2PTransactionGrpcService(enclaveMediator);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(deleteResponseObserver, pushResponseObserver, resendResponseObserver);
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

        com.quorum.tessera.api.model.ResendResponse resendResponse = mock(com.quorum.tessera.api.model.ResendResponse.class);
        when(resendResponse.getPayload()).thenReturn(Optional.empty());
        when(enclaveMediator.resend(any())).thenReturn(resendResponse);

        service.resend(request, resendResponseObserver);

        verify(enclaveMediator).resend(any());

        verify(resendResponseObserver).onNext(any());
        verify(resendResponseObserver).onCompleted();
    }

    @Test
    public void invalidDelete() {

        DeleteRequest request = DeleteRequest.newBuilder()
                .build();

        service.delete(request, deleteResponseObserver);

        verify(deleteResponseObserver).onError(any());

    }

}
