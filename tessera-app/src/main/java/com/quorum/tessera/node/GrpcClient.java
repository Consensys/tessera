package com.quorum.tessera.node;

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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GrpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcClient.class);

    private final ManagedChannel channel;

    private final PartyInfoGrpc.PartyInfoBlockingStub partyInfoBlockingStub;

    private final TransactionGrpc.TransactionBlockingStub transactionBlockingStub;

    private static final ConcurrentHashMap<String, GrpcClient> clients = new ConcurrentHashMap<>();

    private GrpcClient(final ManagedChannel channel) {
        this.channel = channel;
        this.partyInfoBlockingStub = PartyInfoGrpc.newBlockingStub(channel);
        this.transactionBlockingStub = TransactionGrpc.newBlockingStub(channel);
    }

    private GrpcClient(final String targetUrl) {
        this(ManagedChannelBuilder
            .forTarget(targetUrl.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)",""))
            .usePlaintext()
            .build()
        );
    }

    private static GrpcClient newClient(final String targetUrl) {
        final GrpcClient client = new GrpcClient(targetUrl);
        clients.put(targetUrl, client);
        return client;
    }

    public static GrpcClient getClient(final String targetUrl) {
        final GrpcClient client = Optional.ofNullable(clients.get(targetUrl))
            .orElse(newClient(targetUrl));
        return client;
    }

    public byte[] getPartyInfo(final byte[] data) {
        final PartyInfoMessage request = PartyInfoMessage.newBuilder()
            .setPartyInfo(ByteString.copyFrom(data))
            .build();
        try {
            final PartyInfoMessage response = partyInfoBlockingStub.getPartyInfo(request);
            return response.getPartyInfo().toByteArray();
        } catch (StatusRuntimeException ex) {
            LOGGER.error("RPC failed: {0}", ex.getStatus().getCode());
            LOGGER.debug("RPC failed: {0}", ex.getStatus());
        }
        return null;
    }

    public byte[] push(final byte[] data) {
        final PushRequest request = PushRequest.newBuilder()
            .setData(ByteString.copyFrom(data))
            .build();
        try {
            final PushRequest response = transactionBlockingStub.push(request);
            return response.getData().toByteArray();
        } catch (StatusRuntimeException ex) {
            LOGGER.error("RPC failed: {0}", ex.getStatus().getCode());
            LOGGER.debug("RPC failed: {0}", ex.getStatus());
        }
        return null;
    }

    public boolean makeResendRequest(final ResendRequest request) {
        final TransactionGrpc.TransactionBlockingStub stub = TransactionGrpc.newBlockingStub(channel);
        try {
            stub.resend(request);
            return true;
        }
        catch (StatusRuntimeException ex) {
            LOGGER.error("RPC failed: {0}", ex.getStatus().getCode());
            LOGGER.debug("RPC failed: {0}", ex.getStatus());
        }
        return false;
    }

}
