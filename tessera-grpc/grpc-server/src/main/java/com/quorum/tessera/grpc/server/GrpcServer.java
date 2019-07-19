package com.quorum.tessera.grpc.server;

import com.quorum.tessera.server.TesseraServer;
import io.grpc.Server;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class GrpcServer implements TesseraServer {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);

    private final URI uri;

    private final Server server;

    public GrpcServer(URI uri, Server server) {
        this.uri = uri;
        this.server = server;
    }

    @Override
    public void start() throws IOException {
        try {
            server.start();
            LOGGER.info("gRPC server started, listening on " + uri.getPort());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LOGGER.info("*** Shutting down gRPC server");
                    GrpcServer.this.stop();
                    LOGGER.info("*** gRPC server shut down");
                }
            });
        } catch (IOException ex) {
            LOGGER.error("Cannot start gRPC server. See cause ", ex);
            throw ex;
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

}
