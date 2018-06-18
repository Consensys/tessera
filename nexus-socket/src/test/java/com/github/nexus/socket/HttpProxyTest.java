package com.github.nexus.socket;

import com.github.nexus.socket.HttpProxy;
import com.github.nexus.socket.SocketFactory;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;

@Ignore
public class HttpProxyTest {
    
    private SocketFactory mockSocketFactory;
    
    private Socket mockSocket;
    
    @Before
    public void onSetUp() throws IOException {
        this.mockSocket = mock(Socket.class);
        this.mockSocketFactory =  mock(SocketFactory.class);
        when(mockSocketFactory.create(any(URI.class))).thenReturn(mockSocket);
    }
    
    
    @After
    public void onTearDown() {
        verifyNoMoreInteractions(mockSocket,mockSocketFactory);
    }
    
    @Test
    public void testConnection() throws IOException, URISyntaxException {

            URI uri = new URI("http://localhost:8080");
            HttpProxy httpProxy = new HttpProxy(uri,mockSocketFactory);

            OutputStream outputStream = new ObjectOutputStream(new ByteArrayOutputStream());
            when(mockSocket.getOutputStream()).thenReturn(outputStream);

            boolean result = httpProxy.connect();

            assertThat(result).isTrue();
                
            verify(mockSocket).getOutputStream();
            
           

    }
}
