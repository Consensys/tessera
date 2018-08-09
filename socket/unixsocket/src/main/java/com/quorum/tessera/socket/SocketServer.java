package com.quorum.tessera.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Create a server listening on a Unix Domain Socket for http requests. We
 * create a connection to an HTTP server, and act as a proxy between the socket
 * and the HTTP server.
 */
public class SocketServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);

    private final HttpProxyFactory httpProxyFactory;

    private final ExecutorService executor;

    private final ServerSocket server;

    /**
     * Create the unix domain socket and start the listener thread.
     */
    public SocketServer(final Path socketFile,
                        final HttpProxyFactory httpProxyFactory,
                        final ExecutorService executor,
                        final UnixSocketFactory unixSocketFactory) {

        this.httpProxyFactory = Objects.requireNonNull(httpProxyFactory);

        this.executor = Objects.requireNonNull(executor, "Executor service is required");

        try {
            this.server = unixSocketFactory.createServerSocket(socketFile);

            LOGGER.info("IPC server: {}", server);

        } catch (final IOException ex) {
            LOGGER.error("Failed to create Unix Domain Socket: {}", socketFile.toString());
            throw new TesseraSocketException(ex);
        }

    }

    /**
     * Wait for a client connection, then: - create a connection to the HTTP
     * server - read the client HTTP request and forward it to the HTTP server -
     * read the HTTP response and return it to the client Note that Quorum opens
     * a new client connection for each request.
     */
    @Override
    public void run() {

        LOGGER.debug("Waiting for client connection on unix domain socket...");

        try {

            final Socket accept = server.accept();

            final UnixDomainServerSocket udss = new UnixDomainServerSocket(accept);

            LOGGER.debug("Client connection received");

            final SocketHandler handler = new SocketHandler(udss, httpProxyFactory);

            executor.submit(handler);

        } catch (final IOException ex) {
            LOGGER.error("Failed to create Socket");
            throw new TesseraSocketException(ex);
        }

    }

}
