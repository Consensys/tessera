package com.quorum.tessera.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Objects;

/**
 * Provides ability to read/write to the unix socket connection
 */
public class UnixDomainServerSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnixDomainServerSocket.class);

    private final Socket socket;

    public UnixDomainServerSocket(final Socket socket) {
        this.socket = Objects.requireNonNull(socket);
    }

    /**
     * Read HTTP request from the socket
     *
     * @return the request from the socket
     * @throws TesseraSocketException if an IOException is thrown from the underlying stream
     */
    public byte[] read() {
        try {
            final InputStream is = socket.getInputStream();
            return InputStreamUtils.readAllBytes(is);
        } catch (final IOException ex) {
            LOGGER.error("Failed to read from socket");
            throw new TesseraSocketException(ex);
        }
    }

    /**
     * Writes a response payload to the socket
     *
     * @param payload the response to write
     * @throws TesseraSocketException if an IOException is thrown from the underlying stream
     */
    public void write(final byte[] payload) {

        try (final OutputStream os = socket.getOutputStream()) {

            os.write(payload);
            os.flush();

        } catch (final IOException ex) {
            LOGGER.error("Failed to read from Socket");
            throw new TesseraSocketException(ex);
        }
    }

    /**
     * Closes the socket connection entirely
     */
    public void close() {

        try {
            this.socket.close();
        } catch (final IOException ex){
            throw new UncheckedIOException(ex);
        }

    }
}
