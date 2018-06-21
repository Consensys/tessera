package com.github.nexus.socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class HttpProxyTest {

    private SocketFactory mockSocketFactory;

    private Socket mockSocket;

    private URI uri;

    private HttpProxy httpProxy;

    @Before
    public void onSetUp() throws IOException, URISyntaxException {
        this.uri = new URI("http://localhost:8080");

        this.mockSocket = mock(Socket.class);
        this.mockSocketFactory = mock(SocketFactory.class);

        doReturn(mockSocket).when(mockSocketFactory).create(eq(uri));

        this.httpProxy = new HttpProxy(uri, mockSocketFactory);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(mockSocket, mockSocketFactory);
    }

    @Test
    public void testConnection() throws IOException {
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
    public void testConnectionThrowsConnectException() throws IOException {
        ConnectException connectionException = new ConnectException("Sorry Dave I cant let you do that");

        doThrow(connectionException).when(mockSocketFactory).create(uri);

        boolean result = httpProxy.connect();

        assertThat(result).isFalse();

        verify(mockSocketFactory).create(uri);

    }

    @Test
    public void testConnectionThrowsIOException() throws IOException {
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
    public void connectAndThenDisconnect() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        assertThat(httpProxy.connect()).isTrue();

        httpProxy.disconnect();

        verify(mockSocketFactory).create(uri);
        verify(mockSocket).close();
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();

    }

    @Test
    public void connectAndThenDisconnectThrowsIOException() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        doThrow(IOException.class).when(mockSocket).close();

        assertThat(httpProxy.connect()).isTrue();

        httpProxy.disconnect();

        verify(mockSocketFactory).create(uri);
        verify(mockSocket).close();
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();

    }

    @Test
    public void connectAndSend() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        assertThat(httpProxy.connect()).isTrue();

        httpProxy.sendRequest("HELLOW".getBytes());

        assertThat(outputStream.toByteArray()).isEqualTo("HELLOW".getBytes());

        verify(mockSocketFactory).create(uri);
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();
    }

    @Test
    public void connectAndGetResponse() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        InputStream inputStream = new ByteArrayInputStream(new byte[]{1});
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        assertThat(httpProxy.connect()).isTrue();

        byte[] result = httpProxy.getResponse();

        verify(mockSocketFactory).create(uri);
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();
    }

    @Test
    public void outputStreamThrowsException() throws IOException {

        OutputStream outputStream = mock(OutputStream.class);
        doReturn(outputStream).when(mockSocket).getOutputStream();
        doThrow(IOException.class).when(outputStream).flush();

        httpProxy.connect();

        final Throwable throwable = catchThrowable(() -> httpProxy.sendRequest(new byte[]{}));

        assertThat(throwable).isInstanceOf(NexusSocketException.class);

        verify(mockSocketFactory).create(uri);
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();

    }

    @Test
    public void exceptionThrownIfNoDataToRead() throws IOException {

        InputStream inputStream = mock(InputStream.class);
        doReturn(inputStream).when(mockSocket).getInputStream();
        doThrow(IOException.class).when(inputStream).read(any(byte[].class));

        httpProxy.connect();

        final Throwable throwable = catchThrowable(() -> httpProxy.getResponse());

        assertThat(throwable).isInstanceOf(NexusSocketException.class);

        verify(mockSocketFactory).create(uri);
        verify(mockSocket).getInputStream();
        verify(mockSocket).getOutputStream();

    }
}
