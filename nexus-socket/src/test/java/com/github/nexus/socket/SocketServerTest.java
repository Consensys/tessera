package com.github.nexus.socket;

import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class SocketServerTest {

    private SocketServer socketServer;

    private HttpProxyFactory httpProxyFactory;

    private ScheduledExecutorService executorService;

    private UnixSocketFactory unixSocketFactory;

    private Path socketFile;

    private ServerSocket serverSocket;

    private Socket socket;

    @Before
    public void setUp() throws IOException {

        this.socketFile = Paths.get(System.getProperty("java.io.tmpdir"), "junit.txt");

        this.httpProxyFactory = mock(HttpProxyFactory.class);
        this.executorService = mock(ScheduledExecutorService.class);

        this.serverSocket = mock(ServerSocket.class);
        this.socket = mock(Socket.class);

        doReturn(socket).when(serverSocket).accept();

        this.unixSocketFactory = mock(UnixSocketFactory.class);

        doReturn(serverSocket).when(unixSocketFactory).createServerSocket(socketFile);

        this.socketServer = new SocketServer(socketFile, httpProxyFactory, executorService, unixSocketFactory);
    }

    @After
    public void tearDown() throws IOException {
        verifyNoMoreInteractions(httpProxyFactory, executorService);
        Files.deleteIfExists(socketFile);
    }

    /*
    FIXME: The class itself needs refectoring to be easier to test
    */
    @Test
    public void run() throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {

        socketServer.init();

        HttpProxy httpProxy = mock(HttpProxy.class);
        when(httpProxy.connect()).thenReturn(true);

        when(httpProxyFactory.create()).thenReturn(httpProxy);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("SOMEDATA".getBytes());

        when(socket.getInputStream()).thenReturn(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(outputStream);

        when(httpProxy.getResponse()).thenReturn("SOMERESPONSE".getBytes());


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(socketServer);

        TimeUnit.SECONDS.sleep(2L);


        executor.shutdown();

        //Reset as we dont know how many times anuything has been called
        reset(httpProxyFactory);

    }

    @Test
    public void testThrowExceptionWhenCreateSecureHttpProxy() throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {

        socketServer.init();

        HttpProxy httpProxy = mock(HttpProxy.class);
        when(httpProxy.connect()).thenReturn(true);

        when(httpProxyFactory.create()).thenThrow(IOException.class);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("SOMEDATA".getBytes());

        when(socket.getInputStream()).thenReturn(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(outputStream);

        when(httpProxy.getResponse()).thenReturn("SOMERESPONSE".getBytes());


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(socketServer);

        TimeUnit.SECONDS.sleep(2L);


        executor.shutdown();

        //Reset as we dont know how many times anuything has been called
        reset(httpProxyFactory);


    }

    @Test
    public void runThrowsIOExceptionOnClientSocket() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {

        socketServer.init();

        HttpProxy httpProxy = mock(HttpProxy.class);
        when(httpProxy.connect()).thenReturn(true);

        when(httpProxyFactory.create()).thenReturn(httpProxy);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("SOMEDATA".getBytes());

        when(socket.getInputStream()).thenReturn(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(outputStream);

        when(httpProxy.getResponse()).thenReturn("SOMERESPONSE".getBytes());

        doThrow(IOException.class).when(serverSocket).accept();

        final Throwable throwable = catchThrowable(socketServer::run);

        assertThat(throwable).isInstanceOf(NexusSocketException.class).hasCauseExactlyInstanceOf(IOException.class);

    }

    @Test
    public void initServerSocketSucceeds() throws IOException {
        socketServer.init();

        verify(unixSocketFactory).createServerSocket(any(Path.class));
    }

    @Test
    public void initServerFails() throws IOException {
        final IOException exception = new IOException("BANG!!");

        doThrow(exception).when(unixSocketFactory).createServerSocket(any(Path.class));

        final Throwable ex = catchThrowable(socketServer::init);
        assertThat(ex)
            .isInstanceOf(NexusSocketException.class)
            .hasMessageContaining("BANG!!")
            .hasCauseExactlyInstanceOf(IOException.class);

        verify(unixSocketFactory).createServerSocket(any(Path.class));
    }

}
