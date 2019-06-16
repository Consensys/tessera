package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.jaxrs.unixsocket.JerseyUnixSocketConnectorProvider;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;

import java.net.URI;
import java.net.URISyntaxException;

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
    public void testBuildSecureClientCAMode() throws URISyntaxException {

        ServerConfig serverConfig = mock(ServerConfig.class);
        SslConfig sslConfig = mock(SslConfig.class);
        when(serverConfig.isSsl()).thenReturn(true);
        when(serverConfig.getServerUri()).thenReturn(new URI("https://localhost:8080"));
        when(serverConfig.getSslConfig()).thenReturn(sslConfig);
        
        SSLContext sslContext = mock(SSLContext.class);
        when(sslContextFactory.from(serverConfig.getServerUri().toString(),sslConfig)).thenReturn(sslContext);
        
        Client client = factory.buildFrom(serverConfig);
        assertThat(client).isNotNull();

        verify(sslContextFactory).from(serverConfig.getServerUri().toString(),sslConfig);
    }
    
    @Test
    public void createUnixSocketClient() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setServerAddress("unix:/tmp/bogus.socket");
        org.glassfish.jersey.client.JerseyClient result = (org.glassfish.jersey.client.JerseyClient) factory.buildFrom(serverConfig);
        assertThat(result.getConfiguration().getProperty("unixfile")).isNotNull().isInstanceOf(URI.class);
         assertThat(result.getConfiguration().getProperty("unixfile").toString()).isEqualTo("unix:/tmp/bogus.socket");
        
        assertThat(result.getConfiguration().getConnectorProvider()).isInstanceOf(JerseyUnixSocketConnectorProvider.class);
        
    }
    
     @Test
    public void createDefaultInstance() {
        ClientFactory clientFactory = new ClientFactory();
        assertThat(clientFactory).isNotNull();
    }

}
