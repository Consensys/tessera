package com.quorum.tessera.socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SocketHandlerTest {

    private UnixDomainServerSocket unixSocket;

    private HttpProxyFactory httpConnectionFactory;

    private SocketHandler handler;

    @Before
    public void init() {
        this.unixSocket = mock(UnixDomainServerSocket.class);
        this.httpConnectionFactory = mock(HttpProxyFactory.class);

        this.handler = new SocketHandler(unixSocket, httpConnectionFactory);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(unixSocket, httpConnectionFactory);
    }

    @Test
    public void successfulConnection() {

        final HttpProxy httpProxy = mock(HttpProxy.class);
        doReturn(httpProxy).when(httpConnectionFactory).create();
        doReturn(true).when(httpProxy).connect();

        doReturn("IN".getBytes()).when(unixSocket).read();
        doReturn("OUT".getBytes()).when(httpProxy).getResponse();

        handler.run();

        verify(httpConnectionFactory).create();
        verify(httpProxy).connect();

        verify(unixSocket).read();
        verify(httpProxy).getResponse();

        verify(unixSocket).write(eq("OUT".getBytes()));
        verify(httpProxy).sendRequest("IN".getBytes());

        verify(httpProxy).disconnect();
        verify(unixSocket).close();

    }

    @Test
    public void successfulConnectionTakesMultipleAttempts() {

        final HttpProxy httpProxy = mock(HttpProxy.class);
        doReturn(httpProxy).when(httpConnectionFactory).create();
        doReturn(false, true).when(httpProxy).connect();

        doReturn("IN".getBytes()).when(unixSocket).read();
        doReturn("OUT".getBytes()).when(httpProxy).getResponse();

        handler.run();

        verify(httpConnectionFactory).create();
        verify(httpProxy, times(2)).connect();

        verify(unixSocket).read();
        verify(httpProxy).getResponse();

        verify(unixSocket).write(eq("OUT".getBytes()));
        verify(httpProxy).sendRequest("IN".getBytes());

        verify(httpProxy).disconnect();
        verify(unixSocket).close();

    }

    @Test
    public void allErrorsBubbleUp() {

        doThrow(RuntimeException.class).when(httpConnectionFactory).create();

        final Throwable throwable = catchThrowable(handler::run);

        assertThat(throwable).isInstanceOf(RuntimeException.class);

        verify(httpConnectionFactory).create();

    }

}
