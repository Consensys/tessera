package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Create a server listening on a Unix Domain Socket for http requests.
 * We create a connection to an HTTP server, and act as a proxy between the socket and the HTTP server.
 * TODO: should possibly support connections from multiple clients
 */
public class SocketServer extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);;

    private final UnixDomainServerSocket serverUds;

    private HttpProxyFactory httpProxyFactory;

    private HttpProxy httpProxy;

    private final URI serverUri;

    /**
     * Create the unix domain socket and start the listener thread.
     */
    public SocketServer(Configuration config, HttpProxyFactory httpProxyFactory, URI serverUri) {

        Objects.requireNonNull(config);
        Objects.requireNonNull(httpProxyFactory);
        Objects.requireNonNull(serverUri);

        this.httpProxyFactory = httpProxyFactory;
        this.serverUri = serverUri;

        serverUds = new UnixDomainServerSocket();
        serverUds.create(config.workdir(), config.socket());

        this.start();
    }

    /**
     * Run forever, servicing requests received on the socket.
     */
    public void run() {

        while (true) {
            serveSocketRequest();
        }

    }

    /**
     * Wait for a client connection, then:
     * - create a connection to the HTTP server
     * - read the client HTTP request and forward it to the HTTP server
     * - read the HTTP response and return it to the client
     * Note that Quorum opens a new client connection for each request.
     */
    private void serveSocketRequest() {

        try {
            LOGGER.info("Waiting for client connection on unix domain socket...");
            serverUds.connect();
            LOGGER.info("Client connection received");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //Get a connection to the HTTP server.
        if (createHttpServerConnection()) {

            //Read the request from the socket and send it to the HTTP server
            String message = serverUds.read();
            LOGGER.info("Received message on socket: {}", message);
            httpProxy.sendRequest(new String(message));

            //Return the HTTP response to the socket
            String response = httpProxy.getResponse();
            LOGGER.info("Received http response: {}", response);
            serverUds.write(response);

            cleanupHttpServerConnection();
        }

    }

    /**
     * Get a connection to the HTTP Server.
     */
    private boolean createHttpServerConnection() {

        httpProxy = httpProxyFactory.create(serverUri);

        // TODO: add configurable number of attempts, instead of looping forever
        boolean connected = false;
        while (!connected) {
            LOGGER.info("Attempting connection to HTTP server...");
            connected = httpProxy.connect();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.info("Interrupted - exiting");
                return false;
            }
        }
        LOGGER.info("Connected to HTTP server");

        return true;
    }

    /**
     * Clean up the connection to the HTTP Server.
     */
    private void cleanupHttpServerConnection() {
        httpProxy.disconnect();
    }

}
