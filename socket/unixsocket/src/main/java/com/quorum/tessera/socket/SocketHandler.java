package com.quorum.tessera.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Handles a single request from the Unix Socket
 * Run asynchronously to allow the socket server to listen for other connections
 * whilst this request is being dealt with.
 */
public class SocketHandler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);

    private final UnixDomainServerSocket unixSocket;

    private final HttpProxyFactory httpConnectionFactory;

    public SocketHandler(final UnixDomainServerSocket unixSocket, final HttpProxyFactory httpConnectionFactory) {
        this.unixSocket = Objects.requireNonNull(unixSocket);
        this.httpConnectionFactory = Objects.requireNonNull(httpConnectionFactory);
    }

    /**
     * Deals with a server request by reading all the bytes from the request,
     * establishing an HTTP connection to the local server, and writing the bytes.
     *
     * Does the reverse for the response (read from HTTP, write to socket)
     */
    @Override
    public void run() {

        final HttpProxy httpProxy = this.createHttpServerConnection();

        //Read the request from the socket and send it to the HTTP server
        final byte[] message = unixSocket.read();
        LOGGER.debug("Received message on socket: {}", new String(message, UTF_8));
        httpProxy.sendRequest(message);

        //Return the HTTP response to the socket
        final byte[] response = httpProxy.getResponse();
        LOGGER.debug("Received http response: {}", new String(response, UTF_8));
        unixSocket.write(response);

        unixSocket.close();
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
            LOGGER.debug("Attempting connection to HTTP server...");
            connected = httpProxy.connect();
        }

        LOGGER.debug("Connected to HTTP server");

        return httpProxy;
    }

}
