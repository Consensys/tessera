package com.github.nexus.socket;

import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
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
 * TODO: should possibly support connections from multiple clients
 */
public class SocketServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);

    private final HttpProxyFactory httpProxyFactory;

    private HttpProxy httpProxy;

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
            throw new NexusSocketException(ex);
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
        final UnixDomainServerSocket udss;

        LOGGER.info("Waiting for client connection on unix domain socket...");
        try {
            final Socket accept = server.accept();

            udss = new UnixDomainServerSocket(accept);

        } catch (IOException ex) {
            LOGGER.error("Failed to create Socket");
            throw new NexusSocketException(ex);
        }
        LOGGER.info("Client connection received");

        //Get a connection to the HTTP server.
        if (createHttpServerConnection()) {

            //Read the request from the socket and send it to the HTTP server
            byte[] message = udss.read();
            LOGGER.info("Received message on socket: {}", message);
            httpProxy.sendRequest(message);

            //Return the HTTP response to the socket
            byte[] response = httpProxy.getResponse();
            LOGGER.info("Received http response: {}", response);
            udss.write(response);

            httpProxy.disconnect();
        }

    }

    /**
     * Get a connection to the HTTP Server.
     */
    private boolean createHttpServerConnection() {

        try {
            httpProxy = httpProxyFactory.create();
        } catch (Exception ex) {
            return false;
        }

        // TODO: add configurable number of attempts, instead of looping forever
        boolean connected = false;
        while (!connected) {
            LOGGER.info("Attempting connection to HTTP server...");
            connected = httpProxy.connect();
        }
        LOGGER.info("Connected to HTTP server");

        return true;
    }


}
