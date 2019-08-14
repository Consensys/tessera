package com.quorum.tessera.client;

import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.grpc.p2p.Convertor;
import com.quorum.tessera.grpc.p2p.ResendBatchRequest;
import com.quorum.tessera.grpc.p2p.ResendBatchResponse;

import java.util.Objects;

public class GrpcP2pClient implements P2pClient {

    private final GrpcClientFactory grpcClientFactory;

    GrpcP2pClient(GrpcClientFactory grpcClientFactory) {
        this.grpcClientFactory = Objects.requireNonNull(grpcClientFactory);
    }

    public GrpcP2pClient() {
        this(new GrpcClientFactory());
    }

    @Override
    public byte[] push(String targetUrl, byte[] data) {
        return grpcClientFactory.getClient(targetUrl).push(data);
    }

    @Override
    public boolean sendPartyInfo(String targetUrl, byte[] data) {
        return Objects.nonNull(grpcClientFactory.getClient(targetUrl).sendPartyInfo(data));
    }

    @Override
    public boolean makeResendRequest(String targetUrl, Object request) {
        com.quorum.tessera.partyinfo.ResendRequest r = com.quorum.tessera.partyinfo.ResendRequest.class.cast(request);
        com.quorum.tessera.grpc.p2p.ResendRequest grpcObj = Convertor.toGrpc(r);
        return grpcClientFactory.getClient(targetUrl).makeResendRequest(grpcObj);
    }

    @Override
    public boolean pushBatch(String targetUrl, com.quorum.tessera.partyinfo.PushBatchRequest pushBatchRequest) {
        com.quorum.tessera.grpc.p2p.PushBatchRequest grpcObj = Convertor.toGrpc(pushBatchRequest);
        return grpcClientFactory.getClient(targetUrl).pushBatch(grpcObj);
    }

    @Override
    public com.quorum.tessera.partyinfo.ResendBatchResponse makeBatchResendRequest(
            String targetUrl, com.quorum.tessera.partyinfo.ResendBatchRequest request) {
        ResendBatchRequest grpcResendBatchRequest = Convertor.toGrpc(request);
        ResendBatchResponse grpcResponse =
                grpcClientFactory.getClient(targetUrl).makeBatchResendRequest(grpcResendBatchRequest);
        return Convertor.toModel(grpcResponse);
    }
}
