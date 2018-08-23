package com.quorum.tessera.grpc.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class GrpcServer {

    private static final Logger LOGGER = Logger.getLogger(GrpcServer.class.getName());

    private Server server;

    private URI uri;

    private List<BindableService> services;

    public GrpcServer(URI uri, List<BindableService> services) {
        this.uri = uri;
        this.services = services;
        ServerBuilder serverBuilder = ServerBuilder.forPort(uri.getPort());
        services.stream().forEach(serverBuilder::addService);
        this.server = serverBuilder.build();
    }

    public void start() throws IOException {

        server.start();

        LOGGER.info("Server started, listening on " + uri.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


}
