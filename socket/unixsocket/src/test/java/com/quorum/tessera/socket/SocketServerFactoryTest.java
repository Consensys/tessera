package com.quorum.tessera.socket;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SocketServerFactoryTest {

    @Test
    public void testSocketServerFactory() throws Exception {

        final Path socketDir = Files.createTempDirectory(UUID.randomUUID().toString());
        final String socketFile = "junit.txt";
        
        final Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getServerUri()).thenReturn(new URI("http://bogos.com"));
        
        when(config.getUnixSocketFile())
                .thenReturn(Paths.get(socketDir.toString(), socketFile));

        when(config.getServerConfig()).thenReturn(serverConfig);

        final SocketServer result = SocketServerFactory.createSocketServer(config);
        assertThat(result).isNotNull();

    }
    
    @Test(expected = TesseraSocketException.class)
    public void testSocketServerFactoryThrowsException() {
        
        final Config config = mock(Config.class);
        doThrow(UnsupportedOperationException.class).when(config).getUnixSocketFile();
        
        SocketServerFactory.createSocketServer(config);
    }
}
