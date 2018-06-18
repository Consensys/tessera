
package com.github.nexus.socket;

import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import static org.mockito.Mockito.mock;


public class MockUnixSocketFactory implements UnixSocketFactory {
    
    @Override
    public ServerSocket createServerSocket(Path socketFile) throws IOException {
        return mock(ServerSocket.class);
    }

    @Override
    public Socket createSocket(Path socketFile) throws IOException {
        return mock(Socket.class);
    }
    
}
