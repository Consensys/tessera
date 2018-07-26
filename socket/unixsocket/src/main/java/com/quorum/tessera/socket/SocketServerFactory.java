package com.quorum.tessera.socket;

import com.quorum.tessera.config.Config;

import java.util.concurrent.Executors;

/**
 * A factory for creating server sockets based on the provided configuration
 */
public interface SocketServerFactory {

    /**
     * Creates a server with the HTTP proxy target and the location of the socket file
     *
     * @param config the server configuration to use
     * @return the instantiated server that can listen for requests
     */
    static SocketServer createSocketServer(final Config config) {
        try {
            return new SocketServer(
                config.getUnixSocketFile(),
                new HttpProxyFactory(config.getServerConfig()),
                Executors.newCachedThreadPool(),
                UnixSocketFactory.create()
            );
        } catch (final Exception ex) {
            throw new TesseraSocketException(ex);
        }
    }

}
