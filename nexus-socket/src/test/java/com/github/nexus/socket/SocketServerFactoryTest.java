package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SocketServerFactoryTest {

    @Test
    public void testSocketServerFactory() throws Exception {

        final Path socketDir = Files.createTempDirectory(UUID.randomUUID().toString());
        final String socketFile = "junit.txt";
        
        final Configuration config = mock(Configuration.class);
        when(config.uri()).thenReturn(new URI("http://bogos.com"));
        when(config.workdir()).thenReturn(socketDir.toString());
        when(config.socket()).thenReturn(socketFile);
        doReturn("OFF").when(config).tls();
        
        final SocketServer result = SocketServerFactory.createSocketServer(config);
        assertThat(result).isNotNull();

    }
    
}
