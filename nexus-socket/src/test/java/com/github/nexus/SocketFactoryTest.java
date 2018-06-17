package com.github.nexus.socket;

public class SocketFactoryTest {

    @Test
    public void socketIsntNull() {

        final Scoket keyEncryptor = SocketFactory.create();

        assertThat(socket).isNotNull();

    }
}
