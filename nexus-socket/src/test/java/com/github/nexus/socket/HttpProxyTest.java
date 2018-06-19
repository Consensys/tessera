package com.github.nexus.socket;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class HttpProxyTest {

    private SocketFactory mockSocketFactory;

    private Socket mockSocket;

    @Before
    public void onSetUp() throws IOException {
        this.mockSocket = mock(Socket.class);
        this.mockSocketFactory = mock(SocketFactory.class);
        when(mockSocketFactory.create(any(URI.class))).thenReturn(mockSocket);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(mockSocket, mockSocketFactory);
    }

    @Test
    public void testConnection() throws IOException, URISyntaxException {

        URI uri = new URI("http://localhost:8080");
        HttpProxy httpProxy = new HttpProxy(uri, mockSocketFactory);

        OutputStream outputStream = new ObjectOutputStream(new ByteArrayOutputStream());
        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream("SOMDATA".getBytes());

        when(mockSocket.getInputStream()).thenReturn(inputStream);

        boolean result = httpProxy.connect();

        assertThat(result).isTrue();

        verify(mockSocket).getOutputStream();
        verify(mockSocket).getInputStream();
        verify(mockSocketFactory).create(uri);

    }

    @Test
    public void testConnectionThrowsConnectException() throws IOException, URISyntaxException {

        URI uri = new URI("http://localhost:8080");
        HttpProxy httpProxy = new HttpProxy(uri, mockSocketFactory);

        ConnectException connectionException = new ConnectException("Sorry Dave I cant let you do that");

        doThrow(connectionException).when(mockSocketFactory).create(uri);

        boolean result = httpProxy.connect();

        assertThat(result).isFalse();

        verify(mockSocketFactory).create(uri);

    }

    @Test
    public void testConnectionThrowsIOException() throws IOException, URISyntaxException {

        URI uri = new URI("http://localhost:8080");
        HttpProxy httpProxy = new HttpProxy(uri, mockSocketFactory);

        IOException ioexception = new IOException("Sorry Dave I cant let you do that");

        doThrow(ioexception).when(mockSocketFactory).create(uri);

        try {
            httpProxy.connect();
            failBecauseExceptionWasNotThrown(IOException.class);
        } catch (NexusSocketException ex) {
            assertThat(ex).hasCause(ioexception);
        }

        verify(mockSocketFactory).create(uri);

    }

    //Difficult to unit test in isolation due to class design
    @Test
    public void connectAndThenDisconnect() throws URISyntaxException, IOException {
        URI uri = new URI("http://localhost:8080");

        OutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        HttpProxy httpProxy = new HttpProxy(uri, mockSocketFactory);
        assertThat(httpProxy.connect()).isTrue();

        httpProxy.disconnect();

        verify(mockSocketFactory).create(uri);
        verify(mockSocket).close();
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();

    }

    @Test
    public void connectAndThenDisconnectThrowsIOException() throws URISyntaxException, IOException {
        URI uri = new URI("http://localhost:8080");

        OutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        doThrow(IOException.class).when(mockSocket).close();

        HttpProxy httpProxy = new HttpProxy(uri, mockSocketFactory);
        assertThat(httpProxy.connect()).isTrue();

        httpProxy.disconnect();

        verify(mockSocketFactory).create(uri);
        verify(mockSocket).close();
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();

    }
    
    @Test
    public void connectAndSend() throws URISyntaxException, IOException {
        
        URI uri = new URI("http://localhost:8080");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        HttpProxy httpProxy = new HttpProxy(uri, mockSocketFactory);
        assertThat(httpProxy.connect()).isTrue();

        httpProxy.sendRequest("HELLOW");

        assertThat(outputStream.toByteArray()).isEqualTo("HELLOW".getBytes());
        
        verify(mockSocketFactory).create(uri);
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();
    }
    
    
    @Test
    public void connectAndGetResponse() throws URISyntaxException, IOException {
        
        URI uri = new URI("http://localhost:8080");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        HttpProxy httpProxy = new HttpProxy(uri, mockSocketFactory);
        assertThat(httpProxy.connect()).isTrue();

        String result =  httpProxy.getResponse();


        verify(mockSocketFactory).create(uri);
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();
    } 
}
