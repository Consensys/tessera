package com.github.nexus.socket;

import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public class MockUnixSocketFactory implements UnixSocketFactory {

    private ServerSocket serverSocket;
    
    private Socket socket;

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public ServerSocket createServerSocket(Path socketFile) throws IOException {
        return serverSocket;
    }

    @Override
    public Socket createSocket(Path socketFile) throws IOException {
        return socket;
    }

}
