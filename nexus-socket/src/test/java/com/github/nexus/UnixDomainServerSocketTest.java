package com.github.nexus;

import com.github.nexus.socket.UnixDomainServerSocket;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.newsclub.net.unix.AFUNIXServerSocket;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UnixDomainServerSocketTest {

    public UnixDomainServerSocketTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void testServerCreate() {
        final String path = "/tmp";
        final String filename = "tst1.ipc";

        UnixDomainServerSocket uds1 = new UnixDomainServerSocket();
        uds1.create(path, filename);

        /*
         * No way in Java to test that the created file is actually a special file,
         * so we just check that it exists.
         */
        File file = new File(path + "/" + filename);
        assertTrue(file.exists());

        // Check that an attempt to re-create the uds fails
        try {
            uds1.create(path, filename);
            Assertions.failBecauseExceptionWasNotThrown(IOException.class);
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("Address already in use");
        }

    }


    //Ignore for the moment as we need to get the mock(AFUNIXServerSocket) working...
    @Ignore
    public void testServerConnectThrowsException() {
        final String path = "/tmp";
        final String filename = "tst1.ipc";

        UnixDomainServerSocket uds1 = new UnixDomainServerSocket();
        uds1.create(path, filename);

        AFUNIXServerSocket serverSocket = mock(AFUNIXServerSocket.class);

        try {
            doThrow(new IOException("Address already in use")).when(serverSocket).accept();

            uds1.connect();
            Assertions.failBecauseExceptionWasNotThrown(IOException.class);
        } catch (IOException ex) {
            assertThat(ex.getMessage()).contains("Address already in use");
        }

    }

}
