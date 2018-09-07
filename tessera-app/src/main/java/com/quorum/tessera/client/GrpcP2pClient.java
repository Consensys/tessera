
package com.quorum.tessera.client;

import com.quorum.tessera.api.grpc.Convertor;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.node.grpc.GrpcClientFactory;
import java.util.Objects;


public class GrpcP2pClient implements P2pClient {
    
    private final GrpcClientFactory grpcClientFactory;

    public GrpcP2pClient(GrpcClientFactory grpcClientFactory) {
        this.grpcClientFactory = Objects.requireNonNull(grpcClientFactory);
    }
    
    @Override
    public byte[] push(String targetUrl, byte[] data) {
        return grpcClientFactory.getClient(targetUrl).push(data);
    }

    @Override
    public byte[] getPartyInfo(String targetUrl, byte[] data) {
        return grpcClientFactory.getClient(targetUrl).getPartyInfo(data);
    }

    @Override
    public boolean makeResendRequest(String targetUrl, ResendRequest request) {
        com.quorum.tessera.api.grpc.model.ResendRequest grpcObj =  Convertor.toGrpc(request);
        return grpcClientFactory.getClient(targetUrl).makeResendRequest(grpcObj);
    }
    
    
}
