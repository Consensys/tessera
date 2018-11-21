package com.quorum.tessera.config;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class UnixServerSocketTest {

    private UnixServerSocket serverSocket;

    @Before
    public void setUp() {
        serverSocket = new UnixServerSocket("somePath.ipc");
    }

    @Test
    public void getPath() {
        assertThat(serverSocket.getPath()).isEqualTo("somePath.ipc");
    }

    @Test
    public void setPath() {
        serverSocket.setPath("someOtherPath.ipc");
        assertThat(serverSocket.getPath()).isEqualTo("someOtherPath.ipc");
    }

    @Test
    public void getValidServerUri() {
        serverSocket.getServerUri();
    }

    @Test(expected = ConfigException.class)
    public void getInvalidServerUri() {
        serverSocket.setPath("%^$&^%");
        serverSocket.getServerUri();
    }

}
