package com.github.tessera.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * Provide support for servers over a Unix Domain Socket.
 * Uses junixsocket: https://github.com/kohlschutter/junixsocket
 */
public class UnixDomainServerSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnixDomainServerSocket.class);

    private final Socket socket;

    public UnixDomainServerSocket(final Socket socket) {
        this.socket = Objects.requireNonNull(socket);
    }

    /**
     * Read HTTP request from the socket.
     */
    public byte[] read() {
        try {
            final InputStream is = socket.getInputStream();
            return InputStreamUtils.readAllBytes(is);
        } catch (final IOException ex) {
            LOGGER.error("Failed to read from socket");
            throw new NexusSocketException(ex);
        }
    }


    public void write(final byte[] payload) {

        try (final OutputStream os = socket.getOutputStream()) {

            os.write(payload);
            os.flush();

        } catch (final IOException ex) {
            LOGGER.error("Failed to read from Socket");
            throw new NexusSocketException(ex);
        }
    }
}
