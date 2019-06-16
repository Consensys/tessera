package com.quorum.tessera.client;

import com.google.protobuf.ByteString;
import com.quorum.tessera.grpc.p2p.*;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GrpcClientTest {

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final PartyInfoGrpc.PartyInfoImplBase partyInfoService =
        mock(PartyInfoGrpc.PartyInfoImplBase.class,
            delegatesTo(new PartyInfoGrpcServiceDelegate() {
            }));

    private final P2PTransactionGrpc.P2PTransactionImplBase p2pTransactionService =
        mock(P2PTransactionGrpc.P2PTransactionImplBase.class,
            delegatesTo(new TransactionGrpcServiceDelegate() {
            }));

    private GrpcClientImpl client;

    @Before
    public void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();

        grpcCleanup.register(InProcessServerBuilder
            .forName(serverName)
            .directExecutor()
            .addService(partyInfoService)
            .addService(p2pTransactionService)
            .build()
            .start());

        final ManagedChannel channel = grpcCleanup.register(
            InProcessChannelBuilder.forName(serverName).directExecutor().usePlaintext().build());

        client = new GrpcClientImpl(channel);
    }

    @Test
    public void testGetPartyInfo() {

        ArgumentCaptor<PartyInfoMessage> requestCaptor = ArgumentCaptor.forClass(PartyInfoMessage.class);
        final byte[] data = "REQUEST".getBytes();

        client.sendPartyInfo(data);

        verify(partyInfoService).getPartyInfo(requestCaptor.capture(), any());

        assertEquals(ByteString.copyFrom(data), requestCaptor.getValue().getPartyInfo());

    }

    @Test
    public void testGetPartyInfoFailed() throws InterruptedException {
        client.shutdown();
        final byte[] data = "REQUEST".getBytes();
        byte[] response = client.sendPartyInfo(data);
        assertThat(response).isNull();
    }

    @Test
    public void testPush() {

        ArgumentCaptor<PushRequest> requestCaptor = ArgumentCaptor.forClass(PushRequest.class);
        final byte[] data = "REQUEST".getBytes();

        client.push(data);

        verify(p2pTransactionService).push(requestCaptor.capture(), any());

        assertEquals(ByteString.copyFrom(data), requestCaptor.getValue().getData());
    }

    @Test
    public void testPushFailed() throws InterruptedException {
        client.shutdown();
        final byte[] data = "REQUEST".getBytes();
        byte[] response = client.push(data);
        assertThat(response).isNull();
    }

    @Test
    public void testResend() {
        ArgumentCaptor<ResendRequest> requestCaptor = ArgumentCaptor.forClass(ResendRequest.class);
        ResendRequest request = ResendRequest.newBuilder().build();

        boolean result = client.makeResendRequest(request);

        verify(p2pTransactionService).resend(requestCaptor.capture(), any());

        assertEquals(request, requestCaptor.getValue());
        assertThat(result).isTrue();
    }

    @Test
    public void testResendFail() throws InterruptedException {
        client.shutdown();
        ResendRequest request = ResendRequest.newBuilder().build();
        boolean result = client.makeResendRequest(request);
        assertThat(result).isFalse();
    }

    /**
     * Delegate to mock the PartyInfoGrpcService class
     * The focus here is to ensure the client fires requests correctly.
     * The real logic for these services are being tests at service test classes
     */
    private class PartyInfoGrpcServiceDelegate extends PartyInfoGrpc.PartyInfoImplBase {
        @Override
        public void getPartyInfo(PartyInfoMessage request, StreamObserver<PartyInfoMessage> responseObserver) {
            byte[] responseData = "RESPONSE".getBytes();
            final PartyInfoMessage response = PartyInfoMessage.newBuilder()
                .setPartyInfo(ByteString.copyFrom(responseData))
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * Delegate to mock the APITransactionGrpcService class
     * The focus here is to ensure the client fires requests correctly.
     * The real logic for these services are being tests at service test classes
     */
    private class TransactionGrpcServiceDelegate extends P2PTransactionGrpc.P2PTransactionImplBase {
        @Override
        public void push(PushRequest request, StreamObserver<PushRequest> responseObserver) {
            byte[] responseData = "RESPONSE".getBytes();
            final PushRequest response = PushRequest.newBuilder()
                .setData(ByteString.copyFrom(responseData))
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void resend(ResendRequest request, StreamObserver<ResendResponse> responseObserver) {
            byte[] responseData = "RESPONSE".getBytes();
            ResendResponse response = ResendResponse.newBuilder()
                .setData(ByteString.copyFrom(responseData))
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
