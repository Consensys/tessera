package com.github.nexus.socket;

import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import org.assertj.core.api.Assertions;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class UnixDomainServerSocketTest {

    private UnixSocketFactory mockUnixSocketFactory;

    private UnixDomainServerSocket unixDomainServerSocket;

    @Before
    public void setUp() {
        mockUnixSocketFactory = mock(UnixSocketFactory.class);
        unixDomainServerSocket = new UnixDomainServerSocket(mockUnixSocketFactory);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockUnixSocketFactory);
    }

    @Test
    public void testServerCreate() throws IOException {
        final String path = "/tmp";
        final String filename = "tst1.ipc";

        unixDomainServerSocket.create(path, filename);

        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));

    }

    @Test
    public void testServerCreateThrowsIOException() throws Exception {

        final String path = "/tmp";
        final String filename = "tst1.ipc";

        IOException exception = new IOException("BANG!!");

        doThrow(exception).when(mockUnixSocketFactory)
                .createServerSocket(any(Path.class));

        try {
            unixDomainServerSocket.create(path, filename);
            Assertions.failBecauseExceptionWasNotThrown(NexusSocketException.class);

        } catch (NexusSocketException ex) {
            assertThat(ex.getMessage()).contains("BANG!!");
            assertThat(ex).hasCause(exception);
        }
        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));
    }

    @Test
    public void connect() throws IOException {
        ServerSocket serverSocket = mock(ServerSocket.class);

        when(mockUnixSocketFactory.createServerSocket(any(Path.class))).thenReturn(serverSocket);

        Path socketFile = Paths.get(System.getProperty("java.io.tempdir"), "junit.txt");

        unixDomainServerSocket.create(socketFile.toFile().getParent(), socketFile.toFile().getName());

        unixDomainServerSocket.connect();

        verify(serverSocket).accept();
        verifyNoMoreInteractions(serverSocket);
        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));

        Files.deleteIfExists(socketFile);

    }

    @Test
    public void connectThrowsIOException() throws IOException {
        ServerSocket serverSocket = mock(ServerSocket.class);

        when(mockUnixSocketFactory.createServerSocket(any(Path.class))).thenReturn(serverSocket);

        Path socketFile = Paths.get(System.getProperty("java.io.tempdir"), "junit.txt");

        unixDomainServerSocket.create(socketFile.toFile().getParent(), socketFile.toFile().getName());

        doThrow(IOException.class).when(serverSocket).accept();

        try {
            unixDomainServerSocket.connect();
            failBecauseExceptionWasNotThrown(NexusSocketException.class);
        } catch (NexusSocketException ex) {
            assertThat(ex)
                    .hasCauseExactlyInstanceOf(IOException.class);
        }
        verify(serverSocket).accept();
        verifyNoMoreInteractions(serverSocket);
        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));

        Files.deleteIfExists(socketFile);

    }

    @Test
    public void connectAndRead() throws IOException {

        final String data = "HELLOW-99";

        Socket mockSocket = mock(Socket.class);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        when(mockSocket.getInputStream()).thenReturn(inputStream);

        ServerSocket serverSocket = mock(ServerSocket.class);
        when(serverSocket.accept()).thenReturn(mockSocket);

        when(mockUnixSocketFactory.createServerSocket(any(Path.class))).thenReturn(serverSocket);

        Path socketFile = Paths.get(System.getProperty("java.io.tempdir"), "junit.txt");
        unixDomainServerSocket.create(socketFile.toFile().getParent(), socketFile.toFile().getName());

        unixDomainServerSocket.connect();

        final byte[] result = unixDomainServerSocket.read();

        //TODO: Verify that the right padding is correct behaviour
        assertThat(new String(result)).startsWith(data);

        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));

        verify(serverSocket).accept();
        verify(mockSocket).getInputStream();
        verifyNoMoreInteractions(serverSocket, mockSocket);

        Files.deleteIfExists(socketFile);
    }

    @Test
    public void connectAndWrite() throws IOException {

        final String data = "HELLOW-99";

        Socket mockSocket = mock(Socket.class);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        ServerSocket serverSocket = mock(ServerSocket.class);
        when(serverSocket.accept()).thenReturn(mockSocket);

        when(mockUnixSocketFactory.createServerSocket(any(Path.class))).thenReturn(serverSocket);

        Path socketFile = Paths.get(System.getProperty("java.io.tempdir"), "junit.txt");
        unixDomainServerSocket.create(socketFile.toFile().getParent(), socketFile.toFile().getName());

        unixDomainServerSocket.connect();

        unixDomainServerSocket.write(data.getBytes());

        assertThat(data).isEqualTo(new String(outputStream.toByteArray()));

        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));

        verify(serverSocket).accept();
        verify(mockSocket).getOutputStream();
        verifyNoMoreInteractions(serverSocket, mockSocket);

        Files.deleteIfExists(socketFile);
    }

    @Test
    public void connectAndReadThrowsException() throws IOException {

        final String data = "HELLOW-99";

        Socket mockSocket = mock(Socket.class);

        doThrow(IOException.class).when(mockSocket).getInputStream();

        ServerSocket serverSocket = mock(ServerSocket.class);
        when(serverSocket.accept()).thenReturn(mockSocket);

        when(mockUnixSocketFactory.createServerSocket(any(Path.class))).thenReturn(serverSocket);

        Path socketFile = Paths.get(System.getProperty("java.io.tempdir"), "junit.txt");
        unixDomainServerSocket.create(socketFile.toFile().getParent(), socketFile.toFile().getName());
        unixDomainServerSocket.connect();

        try {
            unixDomainServerSocket.read();
            failBecauseExceptionWasNotThrown(NexusSocketException.class);
        } catch(NexusSocketException ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(IOException.class);
        }


        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));

        verify(serverSocket).accept();
        verify(mockSocket).getInputStream();
        verifyNoMoreInteractions(serverSocket, mockSocket);

        Files.deleteIfExists(socketFile);
    }

    @Test
    public void connectAndWriteThrowsException() throws IOException {

        final String data = "HELLOW-99";

        Socket mockSocket = mock(Socket.class);

        doThrow(IOException.class).when(mockSocket).getOutputStream();

        ServerSocket serverSocket = mock(ServerSocket.class);
        when(serverSocket.accept()).thenReturn(mockSocket);

        when(mockUnixSocketFactory.createServerSocket(any(Path.class))).thenReturn(serverSocket);

        Path socketFile = Paths.get(System.getProperty("java.io.tempdir"), "junit.txt");
        unixDomainServerSocket.create(socketFile.toFile().getParent(), socketFile.toFile().getName());
        unixDomainServerSocket.connect();

        try {
            unixDomainServerSocket.write("HELLOW".getBytes());
            failBecauseExceptionWasNotThrown(NexusSocketException.class);
        } catch(NexusSocketException ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(IOException.class);
        }


        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));

        verify(serverSocket).accept();
        verify(mockSocket).getOutputStream();
        verifyNoMoreInteractions(serverSocket, mockSocket);

        Files.deleteIfExists(socketFile);
    }
}
