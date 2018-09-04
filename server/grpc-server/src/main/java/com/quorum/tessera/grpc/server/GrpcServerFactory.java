package com.quorum.tessera.grpc.server;

import io.grpc.BindableService;
import io.grpc.ServerBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface GrpcServerFactory {

    default GrpcServer createGRPCServer(URI uri, Integer port, Set<Object> servicesBeans) {

        final List<BindableService> services = servicesBeans
            .stream()
            .map(o -> (BindableService) o)
            .collect(Collectors.toList());

        final URI serverUri = UriBuilder.fromUri(uri).port(port).build();

        ServerBuilder serverBuilder = ServerBuilder.forPort(port);

        services.stream().forEach(serverBuilder::addService);

        return new GrpcServer(serverUri, serverBuilder.build());

    }

    static GrpcServerFactory create() {
        return new GrpcServerFactory() {};
    }
}
