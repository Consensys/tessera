package com.github.nexus.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/**
 * Provide support for servers over a Unix Domain Socket.
 * Uses junixsocket: https://github.com/kohlschutter/junixsocket
 */
public class UnixDomainServerSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnixDomainServerSocket.class);

    private ServerSocket server;

    private Socket socket;

    public UnixDomainServerSocket(final ServerSocket server) {
        this.server = Objects.requireNonNull(server);
    }

    /**
     * Listen for, and accept connections from clients.
     */
    public void connect() {

        try {
            socket = server.accept();

        } catch (IOException ex) {
            LOGGER.error("Failed to create Socket");
            throw new NexusSocketException(ex);
        }

    }

    /**
     * Read HTTP request from the socket.
     */
    public byte[] read() {
        Objects.requireNonNull(socket, "No client connection to read from");

        try {
            InputStream is = socket.getInputStream();
            return InputStreamUtils.readAllBytes(is);
        } catch (IOException ex) {
            LOGGER.error("Failed to read from socket");
            throw new NexusSocketException(ex);
        }
    }


    public void write(final byte[] payload) {

        Objects.requireNonNull(socket, "No client connection to write to");

        try (final OutputStream os = socket.getOutputStream()) {

            os.write(payload);
            os.flush();

        } catch (final IOException ex) {
            LOGGER.error("Failed to read from Socket");
            throw new NexusSocketException(ex);
        }
    }
}
