package com.quorum.tessera.client;

import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.grpc.p2p.Convertor;

import java.util.Objects;

class GrpcP2pClient implements P2pClient {
    
    private final GrpcClientFactory grpcClientFactory;

    GrpcP2pClient(GrpcClientFactory grpcClientFactory) {
        this.grpcClientFactory = Objects.requireNonNull(grpcClientFactory);
    }

    GrpcP2pClient() {
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
    public boolean makeResendRequest(String targetUrl, com.quorum.tessera.partyinfo.ResendRequest request) {
        com.quorum.tessera.grpc.p2p.ResendRequest grpcObj = Convertor.toGrpc(request);
        return grpcClientFactory.getClient(targetUrl).makeResendRequest(grpcObj);
    }

}
