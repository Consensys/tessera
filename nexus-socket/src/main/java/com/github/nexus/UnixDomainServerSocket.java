package com.github.nexus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide support for servers over a Unix Domain Socket.
 */
public class UnixDomainServerSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnixDomainServerSocket.class);

    private ServerSocket server;
    private Socket socket;


    public UnixDomainServerSocket() {
    }

    /**
     * Create a unix domain socket, using the specified directory + path.
     */
    public void create(final String directory, final String filename) {
        final File socketFile = new File(new File(directory), filename);

        try {
            server = AFUNIXServerSocket.newInstance();
            server.bind(new AFUNIXSocketAddress(socketFile));
            LOGGER.info("server: {}", server);

        } catch (IOException ex) {
            LOGGER.error("Failed to create Unix Domain Socket: {}/{}", directory, filename);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Listen for, and accept connections from clients.
     */
    public void connect() {

        try {
            LOGGER.info("Waiting for connection...");
            socket = server.accept();
            LOGGER.info("Connected: {}", socket);

        } catch (IOException ex) {
            LOGGER.error("Failed to create Socket");
            throw new RuntimeException(ex);
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
            throw new RuntimeException(ex);
        }
    }

    public void write(String payload) {

        Objects.requireNonNull(socket, "No client connection to write to");

        try (OutputStream os = socket.getOutputStream()) {

            os.write(payload.getBytes());
            os.flush();

        } catch (IOException ex) {
            LOGGER.error("Failed to read from Socket");
            throw new RuntimeException(ex);
        }
    }
}
