package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.grpc.p2p.Convertor;

import java.net.URI;
import java.util.Objects;

class GrpcP2pClient implements P2pClient {
    
    private final GrpcClientFactory grpcClientFactory;

    GrpcP2pClient(final GrpcClientFactory grpcClientFactory) {
        this.grpcClientFactory = Objects.requireNonNull(grpcClientFactory);
    }

    @Override
    public byte[] push(final URI target, final byte[] data) {
        return grpcClientFactory.getClient(target.toString()).push(data);
    }

    @Override
    public byte[] getPartyInfo(final URI target, final byte[] data) {
        return grpcClientFactory.getClient(target.toString()).getPartyInfo(data);
    }

    @Override
    public boolean makeResendRequest(final URI target, final ResendRequest request) {
        com.quorum.tessera.grpc.p2p.ResendRequest grpcObj = Convertor.toGrpc(request);
        return grpcClientFactory.getClient(target.toString()).makeResendRequest(grpcObj);
    }

}
