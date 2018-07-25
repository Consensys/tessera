package com.quorum.tessera.socket.client;

import com.quorum.tessera.socket.TesseraSocketException;
import com.quorum.tessera.socket.UnixSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * Provide support for clients over a Unix Domain Socket.
 */
public class UnixDomainClientSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnixDomainClientSocket.class);

    private Socket socket;

    private final UnixSocketFactory unixSocketFactory;
    
    public UnixDomainClientSocket(UnixSocketFactory unixSocketFactory) {
        this.unixSocketFactory = Objects.requireNonNull(unixSocketFactory);
    }

    /**
     * Connect to a unix domain socket, using the specified directory + path.
     * The unix domain socket must exist (i.e. a server must be listening on it).
     */
    public void connect(final String directory, final String filename) {
        final File socketFile = new File(new File(directory), filename);

        try {
            socket = unixSocketFactory.createSocket(socketFile.toPath());
        } catch (IOException ex) {
            LOGGER.error("Cannot connect to server using {}/{}", directory, filename);
            throw new TesseraSocketException(ex);
        }
    }

    public String read() {

        Objects.requireNonNull(socket, "No client connection to read from");

        try (InputStream is = socket.getInputStream()) {

            byte[] buf = new byte[128];
            int read = is.read(buf);
            String response = new String(buf, 0, read);
            LOGGER.info("Received: {}", response);

            return response;

        } catch (IOException ex) {
            LOGGER.error("Failed to read from Socket");
            throw new TesseraSocketException(ex);
        }
    }

    public void write(String payload) {

        Objects.requireNonNull(socket, "No client connection to write to");

        try (OutputStream os = socket.getOutputStream()) {

            os.write(payload.getBytes());
            os.flush();

        } catch (IOException ex) {
            LOGGER.error("Failed to read from Socket");
            throw new TesseraSocketException(ex);
        }
    }
}
