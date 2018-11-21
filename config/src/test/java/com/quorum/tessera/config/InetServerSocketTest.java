package com.quorum.tessera.config;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class InetServerSocketTest {

    private InetServerSocket serverSocket;

    @Before
    public void setUp() {
        serverSocket = new InetServerSocket("localhost", 1234);
    }

    @Test
    public void getFields() {
        assertThat(serverSocket.getHostName()).isEqualTo("localhost");
        assertThat(serverSocket.getPort()).isEqualTo(1234);
    }

    @Test
    public void setPath() {
        serverSocket.setHostName("somehost");
        assertThat(serverSocket.getHostName()).isEqualTo("somehost");
        serverSocket.setPort(123);
        assertThat(serverSocket.getPort()).isEqualTo(123);
    }

    @Test
    public void getValidServerUri() {
        serverSocket.getServerUri();
    }

    @Test(expected = ConfigException.class)
    public void getInvalidServerUri() {
        serverSocket.setHostName("%^$&^%");
        serverSocket.setPort(-1);
        serverSocket.getServerUri();
    }
}
