package com.github.nexus.socket;

import java.io.IOException;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SocketFactoryTest {
    
    private SocketFactory socketFactory;
    
    private URI uri;
    
    @Before
    public void onSetUp() throws IOException {
        Path path = Files.createTempFile("foo", UUID.randomUUID().toString());
        uri = path.toUri();
        socketFactory = new  SocketFactory();
                
    }
    
    @After
    public void onTearDown() throws IOException {
        Files.deleteIfExists(Paths.get(uri));
    }
    
    @Test
    public void socketIsntNull() throws IOException, URISyntaxException {

        final Socket socket = socketFactory.create(uri);

        assertThat(socket).isNotNull();

    }
}
