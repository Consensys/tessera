package com.quorum.tessera.socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class SocketServerTest {

    private SocketServer socketServer;

    private HttpProxyFactory httpProxyFactory;

    private ExecutorService executorService;

    private UnixSocketFactory unixSocketFactory;

    private Path socketFile;

    private ServerSocket serverSocket;

    @Before
    public void setUp() throws IOException {

        final Path tempDirectory = Files.createTempDirectory(UUID.randomUUID().toString());
        this.socketFile = tempDirectory.resolve("junit.txt");

        this.httpProxyFactory = mock(HttpProxyFactory.class);
        this.executorService = mock(ExecutorService.class);

        this.serverSocket = mock(ServerSocket.class);

        final Socket socket = mock(Socket.class);
        doReturn(socket).when(serverSocket).accept();

        this.unixSocketFactory = mock(UnixSocketFactory.class);

        doReturn(serverSocket).when(unixSocketFactory).createServerSocket(socketFile);

        this.socketServer = new SocketServer(socketFile, httpProxyFactory, executorService, unixSocketFactory);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(httpProxyFactory, executorService);
    }

    @Test
    public void connectThrowsIOException() throws IOException {
        doThrow(IOException.class).when(serverSocket).accept();

        final Throwable ex = catchThrowable(socketServer::run);
        assertThat(ex)
            .isInstanceOf(TesseraSocketException.class)
            .hasCauseExactlyInstanceOf(IOException.class);

        verify(serverSocket).accept();
        verifyNoMoreInteractions(serverSocket);
    }

    @Test
    public void run() throws Exception {

        socketServer.run();

        verify(serverSocket).accept();
        verify(executorService).submit(any(SocketHandler.class));
    }

    @Test
    public void runThrowsIOExceptionOnClientSocket() throws Exception {

        doThrow(IOException.class).when(serverSocket).accept();

        final Throwable throwable = catchThrowable(socketServer::run);

        assertThat(throwable)
            .isInstanceOf(TesseraSocketException.class)
            .hasCauseExactlyInstanceOf(IOException.class);

    }

    @Test
    public void initServerFails() throws IOException {
        final IOException exception = new IOException("BANG!!");

        doThrow(exception).when(unixSocketFactory).createServerSocket(any(Path.class));

        final Throwable ex = catchThrowable(
            () -> new SocketServer(socketFile, httpProxyFactory, executorService, unixSocketFactory)
        );

        assertThat(ex)
            .isInstanceOf(TesseraSocketException.class)
            .hasMessageContaining("BANG!!")
            .hasCauseExactlyInstanceOf(IOException.class);
    }

}
