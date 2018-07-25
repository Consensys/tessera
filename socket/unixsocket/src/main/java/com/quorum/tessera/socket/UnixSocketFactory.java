package com.quorum.tessera.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * Interface for different implementations to create server sockets and bind
 * to existing sockets as a client
 */
public interface UnixSocketFactory {

    /**
     * Creates a new server unix socket
     *
     * @param socketFile the location to create the socket file
     * @return the server that will accept requests
     * @throws IOException if the file already exists, or another IO failure occurs
     */
     ServerSocket createServerSocket(Path socketFile) throws IOException;

    /**
     * Binds to a server socket as a client, opening a connection the the server
     *
     * @param socketFile the location of the server socket file to connect to
     * @return the socket connection to read/write to/from
     * @throws IOException if there was a problem connecting to the server
     */
     Socket createSocket(Path socketFile) throws IOException;

    /**
     * Find an implementation of {@link UnixSocketFactory} from the service loader
     * @return an implementation of this interface
     */
     static UnixSocketFactory create() {
         return ServiceLoader.load(UnixSocketFactory.class).iterator().next();
     }
     
}
