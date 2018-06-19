package com.github.nexus.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SocketFactoryTest {
    
    private static final int PORT = 9897;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private ServerSocket serverSocket;
    
    private SocketFactory socketFactory;
    
    private URI uri;
    
    @Before
    public void onSetUp() throws IOException, URISyntaxException {
        
        serverSocket = new ServerSocket(PORT);
        executorService.submit(() -> {
            try {
                BufferedReader reader = 
                        new BufferedReader(new InputStreamReader(serverSocket.accept().getInputStream()));
                while(Objects.nonNull(reader.readLine())) {
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        uri = new URI("http://127.0.0.1:"+ PORT);
        socketFactory = new  SocketFactory();
                
    }
    
    @After
    public void onTearDown() throws IOException {
            serverSocket.close();
            executorService.shutdown();
    }
    
    @Test
    public void socketIsntNull() throws IOException, URISyntaxException {

        final Socket socket = socketFactory.create(uri);

        assertThat(socket).isNotNull();
        assertThat(socket.getPort()).isEqualTo(PORT);

    }
}
