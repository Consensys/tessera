package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

public class ServerConfigTest {

  @Test
  public void serverUri() throws URISyntaxException {
    String serverAddress = "somedomain:8989";
    ServerConfig config =
        new ServerConfig(AppType.P2P, serverAddress, CommunicationType.REST, null, null, null);

    assertThat(config.getServerUri()).isEqualTo(new URI("somedomain:8989"));
    assertThat(config.isSsl()).isFalse();
  }

  @Test
  public void bindingUri() throws URISyntaxException {
    String serverAddress = "somedomain:99";
    ServerConfig config =
        new ServerConfig(
            AppType.P2P,
            serverAddress,
            CommunicationType.REST,
            null,
            null,
            "http://somedomain:9000");
    assertThat(config.getBindingUri()).isEqualTo(new URI("http://somedomain:9000"));
    assertThat(config.isSsl()).isFalse();
  }

  @Test(expected = ConfigException.class)
  public void serverUriInvalidUri() {
    new ServerConfig(AppType.P2P, "&@€~:*&2:-1", CommunicationType.REST, null, null, null)
        .getServerUri();
  }

  @Test(expected = ConfigException.class)
  public void bindingUriInvalidUri() {
    new ServerConfig(AppType.P2P, "&@€~:*&2:-1", CommunicationType.REST, null, null, "&@€~:*&2:")
        .getBindingUri();
  }

  @Test
  public void sslNotNullButTlsFlagOff() {
    final SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getTls()).thenReturn(SslAuthenticationMode.OFF);

    ServerConfig serverConfig =
        new ServerConfig(
            AppType.P2P, "somedomain:8989", CommunicationType.REST, sslConfig, null, null);
    assertThat(serverConfig.isSsl()).isFalse();
  }

  @Test
  public void tlsFlagOn() {
    final SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getTls()).thenReturn(SslAuthenticationMode.STRICT);

    ServerConfig serverConfig =
        new ServerConfig(
            AppType.P2P, "somedomain:8989", CommunicationType.REST, sslConfig, null, null);
    assertThat(serverConfig.isSsl()).isTrue();
  }

  @Test
  public void advertisedUrlIsDifferentToBindAddress() {
    final ServerConfig serverConfig =
        new ServerConfig(
            AppType.P2P,
            "somedomain:8989",
            CommunicationType.REST,
            null,
            null,
            "http://bindingUrl:9999");
    assertThat(serverConfig.getBindingAddress()).isEqualTo("http://bindingUrl:9999");
  }

  @Test
  public void nullAdvertisedUrlIsSameAsBindAddress() {
    final ServerConfig serverConfig =
        new ServerConfig(AppType.P2P, "somedomain:8989", CommunicationType.REST, null, null, null);
    assertThat(serverConfig.getBindingAddress()).isEqualTo("somedomain:8989");
    assertThat(serverConfig.isUnixSocket()).isFalse();
  }

  @Test
  public void unixSocketConfig() {
    final ServerConfig serverConfig =
        new ServerConfig(AppType.P2P, "unix:/bogis.ipc", CommunicationType.REST, null, null, null);
    assertThat(serverConfig.getBindingAddress()).isEqualTo("unix:/bogis.ipc");
    assertThat(serverConfig.isUnixSocket()).isTrue();
  }
}
