package com.quorum.tessera.node.grpc;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class GrpcClientFactory {

    private static final ConcurrentMap<String, GrpcClient> CLIENTS = new ConcurrentHashMap<>();

    private GrpcClientFactory() {

    }

    private static GrpcClient newClient(final String targetUrl) {
        final GrpcClient client = new GrpcClient(targetUrl);
        CLIENTS.put(targetUrl, client);
        return client;
    }

    public static GrpcClient getClient(final String targetUrl) {
        final GrpcClient client = Optional.ofNullable(CLIENTS.get(targetUrl))
            .orElse(newClient(targetUrl));
        return client;
    }

}
