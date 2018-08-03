package com.quorum.tessera.junixsocket.adapter;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultUnixSocketFactoryTest {

    private final DefaultUnixSocketFactory factory = new DefaultUnixSocketFactory();

    @Test
    public void serverSocketGetsCreated() throws IOException {

        final Path serverPath = Files.createTempDirectory("tmp").resolve("sock.sock");

       ServerSocket serverSocket =  factory.createServerSocket(serverPath);

        assertThat(Files.exists(serverPath)).isTrue();

    }

    @Test
    public void clientCanConnectToSocket() throws IOException {

        final Path clientPath = Files.createTempDirectory("tmp").resolve("sock.sock");

        factory.createServerSocket(clientPath);

        factory.createSocket(clientPath);

    }

}
