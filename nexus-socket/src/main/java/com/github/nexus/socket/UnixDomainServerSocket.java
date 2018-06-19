package com.github.nexus.socket;

import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide support for servers over a Unix Domain Socket.
 * Uses junixsocket: https://github.com/kohlschutter/junixsocket
 */
public class UnixDomainServerSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnixDomainServerSocket.class);

    private ServerSocket server;
    
    private Socket socket;

    private  final UnixSocketFactory unixSocketFactory;
    
    public UnixDomainServerSocket(UnixSocketFactory unixSocketFactory) {
        this.unixSocketFactory = Objects.requireNonNull(unixSocketFactory);
    }

    /**
     * Create a unix domain socket, using the specified directory + path.
     */
    public void create(final String directory, final String filename) {
        final Path socketFile = Paths.get(directory, filename);

        try {
            server = unixSocketFactory.createServerSocket(socketFile);
            LOGGER.info("server: {}", server);

        } catch (IOException ex) {
            LOGGER.error("Failed to create Unix Domain Socket: {}/{}", directory, filename);
            throw new NexusSocketException(ex);
        }
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

    // Keeping this code for the moment...
    // could re-implement the solution using 2 threads and basic read(), so it's not limited to HTTP.
//    public String read() {
//
//        Objects.requireNonNull(socket, "No client connection to read from");
//
//        try (InputStream is = socket.getInputStream()) {
//
//            byte[] buf = new byte[128];
//            int read = is.read(buf);
//            String message = new String(buf, 0, read);
//            LOGGER.info("Received: {}", message);
//
//            return message;
//
//        } catch (IOException ex) {
//            LOGGER.error("Failed to read from Socket");
//            throw new RuntimeException(ex);
//        }
//    }


    /**
     * Read HTTP request from the socket.
     */
    public String read() {
        Objects.requireNonNull(socket, "No client connection to read from");

        try {
            InputStream is = socket.getInputStream();
            InputStreamReader httpInputStreamReader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(httpInputStreamReader);

            return HttpMessageUtils.getHttpMessage(bufferedReader);

        } catch (IOException ex) {
            LOGGER.error("Failed to read from socket");
            throw new NexusSocketException(ex);
        }
    }


    public void write(String payload) {

        Objects.requireNonNull(socket, "No client connection to write to");

        try (OutputStream os = socket.getOutputStream()) {

            if (!payload.isEmpty()) {
                os.write(payload.getBytes());
                os.flush();
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to read from Socket");
            throw new NexusSocketException(ex);
        }
    }
}
