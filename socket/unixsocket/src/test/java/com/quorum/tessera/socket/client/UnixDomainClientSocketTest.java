package com.quorum.tessera.socket.client;

import com.quorum.tessera.socket.TesseraSocketException;
import com.quorum.tessera.socket.UnixSocketFactory;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UnixDomainClientSocketTest {

    private UnixDomainClientSocket unixDomainClientSocket;

    private UnixSocketFactory unixSocketFactory;

    private Socket socket;

    @Before
    public void setUp() throws Exception {
        unixSocketFactory = mock(UnixSocketFactory.class);
        socket = mock(Socket.class);
        when(unixSocketFactory.createSocket(any(Path.class))).thenReturn(socket);

        unixDomainClientSocket = new UnixDomainClientSocket(unixSocketFactory);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(unixSocketFactory, socket);
    }

    @Test
    public void connect() throws IOException {
        String filename = "somefile.bogus";
        String directory = "/somepath";

        unixDomainClientSocket.connect(directory, filename);

        verify(unixSocketFactory).createSocket(any(Path.class));

    }

    @Test
    public void connectThrowsIOException() throws IOException {
        String filename = "somefile.bogus";
        String directory = "/somepath";

        String message = "SOME PUNK'S BUSTED UP MY RIDE!!";

        IOException exception = new IOException(message);

        doThrow(exception).when(unixSocketFactory).createSocket(any(Path.class));

        try {
            unixDomainClientSocket.connect(directory, filename);
            Assertions.failBecauseExceptionWasNotThrown(TesseraSocketException.class);
        } catch (TesseraSocketException ex) {
            assertThat(ex).hasMessageContaining(message);
            assertThat(ex).hasCause(exception);
        }
        verify(unixSocketFactory).createSocket(any(Path.class));

    }

    @Test
    public void connectAndRead() throws IOException {

        //TODO: Need to untangle
        unixDomainClientSocket.connect("", "");

        final String data = "HELLOW";

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        when(socket.getInputStream()).thenReturn(inputStream);

        final String result = unixDomainClientSocket.read();

        assertThat(result).isEqualTo(data);

        verify(socket).getInputStream();

        verify(unixSocketFactory).createSocket(any(Path.class));

    }

    @Test
    public void connectAndWrite() throws IOException {
        //TODO: Need to untangle
        unixDomainClientSocket.connect("", "");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getOutputStream()).thenReturn(outputStream);

        String data = "I LOVE SPARROWS!!";

        unixDomainClientSocket.write(data);

        assertThat(outputStream.toByteArray()).isEqualTo(data.getBytes());

        verify(socket).getOutputStream();

        verify(unixSocketFactory).createSocket(any(Path.class));

    }

    @Test
    public void connectAndReadThrowsException() throws IOException {

        //TODO: Need to untangle
        unixDomainClientSocket.connect("", "");

        IOException exception = new IOException("OUCH");
        doThrow(exception).when(socket).getInputStream();

        try {
            unixDomainClientSocket.read();
        } catch (TesseraSocketException ex) {
            assertThat(ex).hasCause(exception);
        }
        verify(socket).getInputStream();

        verify(unixSocketFactory).createSocket(any(Path.class));

    }

    @Test
    public void connectAndWriteThrowsException() throws IOException {

        //TODO: Need to untangle
        unixDomainClientSocket.connect("", "");

        IOException exception = new IOException("OUCH");
        doThrow(exception).when(socket).getOutputStream();

        try {
            unixDomainClientSocket.write("BOGUS");
        } catch (TesseraSocketException ex) {
            assertThat(ex).hasCause(exception);
        }
        verify(socket).getOutputStream();

        verify(unixSocketFactory).createSocket(any(Path.class));

    }

}
