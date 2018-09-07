package com.quorum.tessera.node.grpc;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GrpcClientFactory {

    private static final ConcurrentMap<String, GrpcClient> CLIENTS = new ConcurrentHashMap<>();

    public GrpcClientFactory() {

    }

    private static GrpcClient newClient(final String targetUrl) {
        final GrpcClient client = new GrpcClientImpl(targetUrl);
        CLIENTS.put(targetUrl, client);
        return client;
    }

    public GrpcClient getClient(final String targetUrl) {
        final GrpcClient client = Optional.ofNullable(CLIENTS.get(targetUrl))
            .orElse(newClient(targetUrl));
        return client;
    }

}
