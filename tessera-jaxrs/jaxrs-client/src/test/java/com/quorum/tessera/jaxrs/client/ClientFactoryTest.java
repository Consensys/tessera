package com.quorum.tessera.jaxrs.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import jakarta.ws.rs.client.Client;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    when(serverConfig.getProperties()).thenReturn(Collections.emptyMap());

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
    Map<String, String> props = new HashMap<>();
    props.put("partyInfoInterval", "20000");
    when(serverConfig.getProperties()).thenReturn(props);

    SSLContext sslContext = mock(SSLContext.class);
    when(sslContextFactory.from(serverConfig.getServerUri().toString(), sslConfig))
        .thenReturn(sslContext);

    Client client = factory.buildFrom(serverConfig);
    assertThat(client).isNotNull();
    Map clientProperties =
        client.target(serverConfig.getServerUri()).getConfiguration().getProperties();

    assertThat(clientProperties.get(ClientProperties.READ_TIMEOUT)).isEqualTo(15000);
    assertThat(clientProperties.get(ClientProperties.CONNECT_TIMEOUT)).isEqualTo(15000);
    verify(sslContextFactory).from(serverConfig.getServerUri().toString(), sslConfig);
  }

  @Test
  public void createUnixSocketClient() {
    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setServerAddress("unix:/tmp/bogus.socket");
    serverConfig.setApp(AppType.Q2T);
    serverConfig.setCommunicationType(CommunicationType.REST);

    org.glassfish.jersey.client.JerseyClient result =
        (org.glassfish.jersey.client.JerseyClient) factory.buildFrom(serverConfig);
    assertThat(result.getConfiguration().getProperty("unixfile"))
        .isNotNull()
        .isInstanceOf(URI.class);
    assertThat(result.getConfiguration().getProperty("unixfile").toString())
        .isEqualTo("unix:/tmp/bogus.socket");

    assertThat(result.getConfiguration().getConnectorProvider().getClass().getName())
        .isEqualTo("com.quorum.tessera.jaxrs.unixsocket.JerseyUnixSocketConnectorProvider");
  }

  @Test
  public void createDefaultInstance() {
    ClientFactory clientFactory = new ClientFactory();
    assertThat(clientFactory).isNotNull();
  }
}
