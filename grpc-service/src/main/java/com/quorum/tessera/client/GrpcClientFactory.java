package com.quorum.tessera.client;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class GrpcClientFactory {

    private static final ConcurrentMap<String, GrpcClient> CLIENTS = new ConcurrentHashMap<>();

    private static GrpcClient newClient(final String targetUrl) {
        final GrpcClient client = new GrpcClientImpl(targetUrl);
        CLIENTS.put(targetUrl, client);
        return client;
    }

    public GrpcClient getClient(final String targetUrl) {
        final GrpcClient client = CLIENTS.get(targetUrl);
        if (Objects.nonNull(client)) {
            return client;
        }
        return newClient(targetUrl);
    }

}
