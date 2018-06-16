package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Create a server listening on a Unix Domain Socket and processing http requests
 * received over the socket.
 * The http requests are sent to the local http server, and responses are sent
 * back to the socket.
 * TODO: if client disconects then we don't listen for a reconnect
 * TODO: should possibly allow multiple clients to connect
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
     * Get a connection to the HTTP Server, then loop forever, servicing socket requests.
     */
    public void run() {

        while (true) {
            serveSocketRequest();
        }

    }

    /**
     * Get a connection to the HTTP Server.
     */
    public void createHttpServerConnection() {

        httpProxy = httpProxyFactory.create(serverUri);

        // Need to wait until we connect to the HTTP server
        boolean connected = false;
        while (!connected) {
            LOGGER.info("Attempting connection to HTTP server...");
            connected = httpProxy.connect();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.info("Interrupted - exiting");
                return;
            }
        }
        LOGGER.info("Connected to HTTP server");

    }

    /**
     * Listen for a client connection, then act as the interface to the HTTP proxy for this request.
     * Note that Quorum opens a new connection for each request.
     */
    public void serveSocketRequest() {

        try {
            //wait for a client to connect to the socket
            LOGGER.info("Waiting for client connection on unix domain socket...");
            serverUds.connect();
            LOGGER.info("Client connection received");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //Get a connection to the HTTP server.
        createHttpServerConnection();

        //Read the request from the socket and send it to the HTTP server
        String message = serverUds.read();
        LOGGER.info("Received message on socket: {}", message);
        httpProxy.sendRequest(new String(message));

        //Return the HTTP response to the socket
        String response = httpProxy.getResponse();
        LOGGER.info("Received http response: {}", response);
        serverUds.write(response);


    }

}
