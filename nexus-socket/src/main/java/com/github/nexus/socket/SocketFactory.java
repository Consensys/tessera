package com.github.nexus.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

/**
 * Factory class used so that we can mock it for Socket unit tests.
 */
public class SocketFactory {

    public static Socket create(URI serverUri) throws IOException {
        return new Socket(serverUri.getHost(), serverUri.getPort());
    }
}
