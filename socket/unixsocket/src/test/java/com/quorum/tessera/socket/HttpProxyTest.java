package com.quorum.tessera.socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.SocketFactory;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Java6Assertions.assertThat;
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
        this.mockSocketFactory = mock(javax.net.SocketFactory.class);

        doReturn(mockSocket).when(mockSocketFactory).createSocket(eq(uri.getHost()), eq(uri.getPort()));

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

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());
    }

    @Test
    public void testConnectionThrowsConnectException() throws IOException {
        ConnectException connectionException = new ConnectException("Sorry Dave I cant let you do that");

        doThrow(connectionException).when(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());

        boolean result = httpProxy.connect();

        assertThat(result).isFalse();

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());

    }

    @Test
    public void testConnectionThrowsIOException() throws IOException {
        IOException ioexception = new IOException("Sorry Dave I cant let you do that");

        doThrow(ioexception).when(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());

        final Throwable throwable = catchThrowable(httpProxy::connect);
        assertThat(throwable).hasCause(ioexception);

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());

    }

    @Test
    public void connectAndThenDisconnect() throws IOException {
        assertThat(httpProxy.connect()).isTrue();

        httpProxy.disconnect();

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());
        verify(mockSocket).close();
    }

    @Test
    public void connectAndThenDisconnectThrowsIOException() throws IOException {
        doThrow(IOException.class).when(mockSocket).close();

        assertThat(httpProxy.connect()).isTrue();

        httpProxy.disconnect();

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());
        verify(mockSocket).close();
    }

    @Test
    public void connectAndSend() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        doReturn(outputStream).when(mockSocket).getOutputStream();

        assertThat(httpProxy.connect()).isTrue();

        httpProxy.sendRequest("HELLOW".getBytes());

        assertThat(outputStream.toByteArray()).isEqualTo("HELLOW".getBytes());

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());
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

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());
        verify(mockSocket).getInputStream();
    }

    @Test
    public void outputStreamThrowsException() throws IOException {

        OutputStream outputStream = mock(OutputStream.class);
        doReturn(outputStream).when(mockSocket).getOutputStream();
        doThrow(IOException.class).when(outputStream).flush();

        httpProxy.connect();

        final Throwable throwable = catchThrowable(() -> httpProxy.sendRequest(new byte[]{}));

        assertThat(throwable).isInstanceOf(TesseraSocketException.class);

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());
        verify(mockSocket).getOutputStream();

    }

    @Test
    public void exceptionThrownIfNoDataToRead() throws IOException {

        InputStream inputStream = mock(InputStream.class);
        doReturn(inputStream).when(mockSocket).getInputStream();
        doThrow(IOException.class).when(inputStream).read(any(byte[].class));

        httpProxy.connect();

        final Throwable throwable = catchThrowable(() -> httpProxy.getResponse());

        assertThat(throwable).isInstanceOf(TesseraSocketException.class);

        verify(mockSocketFactory).createSocket(uri.getHost(), uri.getPort());
        verify(mockSocket).getInputStream();
    }
}
