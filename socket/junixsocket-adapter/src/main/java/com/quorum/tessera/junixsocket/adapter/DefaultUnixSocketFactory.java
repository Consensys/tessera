package com.quorum.tessera.junixsocket.adapter;

import com.quorum.tessera.socket.UnixSocketFactory;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

/**
 * Creates/binds Unix Sockets using the JUnixSocket implementation
 * https://github.com/kohlschutter/junixsocket
 */
public class DefaultUnixSocketFactory implements UnixSocketFactory {

    @Override
    public ServerSocket createServerSocket(final Path socketFile) throws IOException {
        final ServerSocket server = AFUNIXServerSocket.newInstance();
        server.bind(new AFUNIXSocketAddress(socketFile.toFile()));
        return server;
    }

    @Override
    public Socket createSocket(final Path socketFile) throws IOException {
        final Socket socket = AFUNIXSocket.newInstance();
        socket.connect(new AFUNIXSocketAddress(socketFile.toFile()));
        return socket;
    }

}
