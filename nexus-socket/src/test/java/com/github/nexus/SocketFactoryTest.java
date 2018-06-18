package com.github.nexus;

import com.github.nexus.socket.HttpProxy;
import com.github.nexus.socket.SocketFactory;
import org.junit.Ignore;

import java.net.Socket;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class SocketFactoryTest {

    @Ignore
    public void socketIsntNull() {

        try {
            Socket mockSocket = mock(Socket.class);
            URI uri = new URI("http://localhost:8080");
            HttpProxy httpProxy = new HttpProxy(uri);
            final Socket socket = SocketFactory.create(uri);

            assertThat(socket).isNotNull();

        } catch (Exception ex) {
            fail("Unexpected exception thrown", ex);

        }

    }
}
