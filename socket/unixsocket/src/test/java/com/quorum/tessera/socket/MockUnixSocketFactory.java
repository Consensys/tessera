package com.quorum.tessera.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;

public class MockUnixSocketFactory implements UnixSocketFactory {

    @Override
    public ServerSocket createServerSocket(final Path socketFile) {
        return mock(ServerSocket.class);
    }

    @Override
    public Socket createSocket(final Path socketFile) {
        return mock(Socket.class);
    }

}
