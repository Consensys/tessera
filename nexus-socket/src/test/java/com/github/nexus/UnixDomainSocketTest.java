package com.github.nexus;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class UnixDomainSocketTest {

    public UnixDomainSocketTest() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testItAll() {
        UnixDomainServerSocket uds1 = new UnixDomainServerSocket();
        uds1.create("/tmp", "tst1.ipc");
        uds1.connect();

        //write on serverUds
        uds1.write(new String("wrote message on serverUds"));

        //read it back
        String line = uds1.read();
        System.out.println("Received message: " + line);
    }

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

        // Check that an attempt to re-create a uds fails
        try {
            uds1.create(path, filename);
            Assertions.failBecauseExceptionWasNotThrown(IOException.class);
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("Address already in use");
        }

    }
}
