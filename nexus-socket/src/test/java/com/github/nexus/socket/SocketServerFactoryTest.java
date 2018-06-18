package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import java.net.URI;
import java.net.URISyntaxException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class SocketServerFactoryTest {
    
    public SocketServerFactoryTest() {
    }

    @Test
    public void testSocketServerFactory() throws URISyntaxException {
        Configuration config = mock(Configuration.class);
        when(config.uri()).thenReturn(new URI("http://bogos.com"));
        SocketServer result = SocketServerFactory.createSocketServer(config);
        assertThat(result).isNotNull();
    }
    
}
