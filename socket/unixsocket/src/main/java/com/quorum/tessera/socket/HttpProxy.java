package com.quorum.tessera.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.util.Objects;

/**
 * Proxy that acts as an interface to an HTTP Server.
 * Provides methods for creating the HTTP connection, writing a request and receiving the response.
 */
public class HttpProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxy.class);

    private final URI serverUri;

    private Socket socket;

    private final SocketFactory socketFactory;

    /**
     * Connect to specified URL and create read/sendRequest streams.
     */
    public HttpProxy(final URI serverUri, final javax.net.SocketFactory socketFactory) {
        this.socketFactory = Objects.requireNonNull(socketFactory);
        this.serverUri = Objects.requireNonNull(serverUri);
    }

    /**
     * Connect to the HTTP server.
     */
    public boolean connect() {
        try {

            socket = socketFactory.createSocket(serverUri.getHost(), serverUri.getPort());

            return true;

        } catch (ConnectException ex) {
            return false;

        } catch (Exception ex) {
            LOGGER.error("Failed to connect to URL: {}", serverUri);
            throw new TesseraSocketException(ex);
        }
    }

    /**
     * Disconnect from HTTP server and clean up.
     */
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException ex) {
            LOGGER.info("Ignoring exception on HttpProxy disconnect: {}", ex.getMessage());
        }
    }

    /**
     * Write data to the http connection.
     */
    public void sendRequest(final byte[] data) {

        LOGGER.debug("Sending HTTP request: {}", data);

        try {
            final OutputStream os = socket.getOutputStream();

            os.write(data);
            os.flush();
        } catch (IOException ex) {
            LOGGER.error("Failed to write to socket");
            throw new TesseraSocketException(ex);
        }

    }

    /**
     * Read response from the http connection.
     * Note that an http response will consist of multiple lines.
     */
    public byte[] getResponse() {
        try {
            final InputStream is = socket.getInputStream();

            return InputStreamUtils.readAllBytes(is);
        } catch (IOException ex) {
            LOGGER.error("Failed to read from http socket");
            throw new TesseraSocketException(ex);
        }

    }

}
