package com.github.nexus.junixsocket.adapter;

import com.github.nexus.socket.UnixSocketFactory;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public class DefaultUnixSocketFactory implements UnixSocketFactory {

    public DefaultUnixSocketFactory() {
        new DependencyInstaller().installDependencies();
    }

    @Override
    public ServerSocket createServerSocket(Path socketFile) throws IOException {
        ServerSocket server = AFUNIXServerSocket.newInstance();
        server.bind(new AFUNIXSocketAddress(socketFile.toFile()));
        return server;
    }

    @Override
    public Socket createSocket(Path socketFile) throws IOException {
        Socket socket = AFUNIXSocket.newInstance();
        socket.connect(new AFUNIXSocketAddress(socketFile.toFile()));
        return socket;
    }
}
