package com.github.nexus;

import com.github.nexus.socket.HttpProxy;
import com.github.nexus.socket.SocketFactory;
import org.junit.Ignore;

import java.io.*;
import java.net.Socket;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class HttpProxyTest {

    @Ignore
    public void testConnection() {

        try {
            Socket mockSocket = mock(Socket.class);

            SocketFactory mockSocketFactory = mock(SocketFactory.class);
            when(mockSocketFactory.create(any(URI.class))).thenReturn(mockSocket);


            URI uri = new URI("http://localhost:8080");
            HttpProxy httpProxy = new HttpProxy(uri);

            OutputStream outputStream = new ObjectOutputStream(new ByteArrayOutputStream());
            when(mockSocket.getOutputStream()).thenReturn(outputStream);

            boolean result = httpProxy.connect();

            assertThat(result).isTrue();
        } catch (Exception ex) {
            fail("Unexpected exception thrown", ex);
        }

    }
}
