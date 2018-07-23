package com.github.tessera.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SocketHandler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);

    private final UnixDomainServerSocket unixSocket;

    private final HttpProxyFactory httpConnectionFactory;

    public SocketHandler(final UnixDomainServerSocket unixSocket, final HttpProxyFactory httpConnectionFactory) {
        this.unixSocket = Objects.requireNonNull(unixSocket);
        this.httpConnectionFactory = Objects.requireNonNull(httpConnectionFactory);
    }

    @Override
    public void run() {

        final HttpProxy httpProxy = this.createHttpServerConnection();

        //Read the request from the socket and send it to the HTTP server
        final byte[] message = unixSocket.read();
        LOGGER.info("Received message on socket: {}", new String(message, UTF_8));
        httpProxy.sendRequest(message);

        //Return the HTTP response to the socket
        final byte[] response = httpProxy.getResponse();
        LOGGER.info("Received http response: {}", new String(response, UTF_8));
        unixSocket.write(response);

        httpProxy.disconnect();

    }

    /**
     * Create a connection to the HTTP/S server
     *
     * @return the connection HTTP/S connection to send the data over
     */
    private HttpProxy createHttpServerConnection() {

        final HttpProxy httpProxy = httpConnectionFactory.create();

        // TODO: add configurable number of attempts, instead of looping forever
        boolean connected = false;
        while (!connected) {
            LOGGER.info("Attempting connection to HTTP server...");
            connected = httpProxy.connect();
        }

        LOGGER.info("Connected to HTTP server");

        return httpProxy;
    }

}
