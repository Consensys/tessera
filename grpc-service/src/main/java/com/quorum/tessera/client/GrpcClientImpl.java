package com.quorum.tessera.client;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.grpc.PartyInfoGrpc;
import com.quorum.tessera.api.grpc.TransactionGrpc;
import com.quorum.tessera.api.grpc.model.PartyInfoMessage;
import com.quorum.tessera.api.grpc.model.PushRequest;
import com.quorum.tessera.api.grpc.model.ResendRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

final class GrpcClientImpl implements GrpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcClientImpl.class);

    private final ManagedChannel channel;

    private final PartyInfoGrpc.PartyInfoBlockingStub partyInfoBlockingStub;

    private final TransactionGrpc.TransactionBlockingStub transactionBlockingStub;


    GrpcClientImpl(final ManagedChannel channel) {
        this.channel = channel;
        this.partyInfoBlockingStub = PartyInfoGrpc.newBlockingStub(channel);
        this.transactionBlockingStub = TransactionGrpc.newBlockingStub(channel);
    }

    GrpcClientImpl(final String targetUrl) {
        this(ManagedChannelBuilder
            .forTarget(targetUrl.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)",""))
            .usePlaintext()
            .keepAliveWithoutCalls(true)
            .build()
        );
    }

    @Override
    public byte[] getPartyInfo(final byte[] data) {
        final PartyInfoMessage request = PartyInfoMessage.newBuilder()
            .setPartyInfo(ByteString.copyFrom(data))
            .build();
        try {
            final PartyInfoMessage response = partyInfoBlockingStub.getPartyInfo(request);
            return response.getPartyInfo().toByteArray();
        } catch (StatusRuntimeException ex) {
            LOGGER.error("RPC failed: {}", ex.getStatus().getCode());
            LOGGER.debug("RPC failed: {}", ex.getStatus());
        }
        return null;
    }

    @Override
    public byte[] push(final byte[] data) {
        final PushRequest request = PushRequest.newBuilder()
            .setData(ByteString.copyFrom(data))
            .build();
        try {
            final PushRequest response = transactionBlockingStub.push(request);
            return response.getData().toByteArray();
        } catch (StatusRuntimeException ex) {
            LOGGER.error("RPC failed: {}", ex.getStatus().getCode());
            LOGGER.debug("RPC failed: {}", ex.getStatus());
        }
        return null;
    }

    @Override
    public boolean makeResendRequest(final ResendRequest request) {
        try {
            transactionBlockingStub.resend(request);
            return true;
        }
        catch (StatusRuntimeException ex) {
            LOGGER.error("RPC failed: {}", ex.getStatus().getCode());
            LOGGER.debug("RPC failed: {}", ex.getStatus());
        }
        return false;
    }

    void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

}
