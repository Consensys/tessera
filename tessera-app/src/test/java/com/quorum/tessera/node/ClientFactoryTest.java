package com.quorum.tessera.node;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        
        SSLContext sslContext = mock(SSLContext.class);
        when(sslContextFactory.from(sslConfig)).thenReturn(sslContext);
        
        Client client = factory.buildFrom(serverConfig);
        assertThat(client).isNotNull();

        verify(sslContextFactory).from(sslConfig);
    }

}
