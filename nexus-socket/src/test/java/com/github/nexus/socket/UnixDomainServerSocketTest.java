package com.github.nexus.socket;

import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UnixDomainServerSocketTest {

    private UnixSocketFactory mockUnixSocketFactory;
    
    private UnixDomainServerSocket unixDomainServerSocket;
    
    public UnixDomainServerSocketTest() {
    }

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
            
        } catch(NexusSocketException ex) {
            assertThat(ex.getMessage()).contains("BANG!!");
            assertThat(ex).hasCause(exception);
        }
        verify(mockUnixSocketFactory).createServerSocket(any(Path.class));
    }



}
