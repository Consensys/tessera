package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SocketServerFactoryTest {
    
    public SocketServerFactoryTest() {
    }

    @Test
    public void testSocketServerFactory() throws URISyntaxException, IOException {
        
        Path socketFile = Paths.get(System.getProperty("java.io.tempdir"), "junit.txt");
        
        Configuration config = mock(Configuration.class);
        when(config.uri()).thenReturn(new URI("http://bogos.com"));
        when(config.workdir()).thenReturn(socketFile.toFile().getParent());
        when(config.socket()).thenReturn(socketFile.toFile().getName());
        
        
        SocketServer result = SocketServerFactory.createSocketServer(config);
        assertThat(result).isNotNull();
        
        Files.deleteIfExists(socketFile);
        
    }
    
}
