package com.github.nexus.node;

import com.github.nexus.config.ServerConfig;
import com.github.nexus.config.SslConfig;
import com.github.nexus.ssl.SSLContextFactory;
import org.junit.*;

import javax.ws.rs.client.Client;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ClientFactoryTest {

    private SSLContextFactory sslContextFactory;

    private ClientFactory factory;

    @Before
    public void setUp() {
        sslContextFactory = mock(SSLContextFactory.class);
        factory = new ClientFactory(sslContextFactory);

    }

    @After
    public void after() {
        verifyNoMoreInteractions(sslContextFactory);
    }

    @Test
    public void testBuildInsecureClient() {

        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.isSsl()).thenReturn(false);

        Client client = factory.buildFrom(serverConfig);
        assertThat(client).isNotNull();

    }

    @Test
    public void testBuildSecureClientCAMode() {

        ServerConfig serverConfig = mock(ServerConfig.class);
        SslConfig sslConfig = mock(SslConfig.class);
        when(serverConfig.isSsl()).thenReturn(true);
        when(serverConfig.getSslConfig()).thenReturn(sslConfig);
        
        Client client = factory.buildFrom(serverConfig);
        assertThat(client).isNotNull();

        verify(sslContextFactory).from(sslConfig);
    }

}
