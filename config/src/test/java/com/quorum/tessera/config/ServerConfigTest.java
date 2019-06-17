package com.quorum.tessera.config;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerConfigTest {

    @Test
    public void serverUri() throws URISyntaxException {
        String serverAddress = "somedomain:8989";
        ServerConfig config = new ServerConfig(AppType.P2P, true, serverAddress, CommunicationType.REST, null, null, null, "12345");

        assertThat(config.getServerUri()).isEqualTo(new URI("somedomain:8989"));
        assertThat(config.isSsl()).isFalse();
    }

    @Test
    public void bindingUri() throws URISyntaxException {
        String serverAddress = "somedomain:99";
        ServerConfig config = new ServerConfig(AppType.P2P, true, serverAddress, CommunicationType.REST, null, null, "http://somedomain:9000", "12345");
        assertThat(config.getBindingUri()).isEqualTo(new URI("http://somedomain:9000"));
        assertThat(config.isSsl()).isFalse();
    }

    @Test(expected = ConfigException.class)
    public void serverUriInvalidUri() {
        new ServerConfig(AppType.P2P, true, "&@€~:*&2:-1", CommunicationType.REST, null, null, null, "12345").getServerUri();
    }

    @Test(expected = ConfigException.class)
    public void bindingUriInvalidUri() {
        new ServerConfig(AppType.P2P, true, "&@€~:*&2:-1", CommunicationType.REST, null, null, "&@€~:*&2:", "12345").getBindingUri();
    }

    @Test
    public void sslNotNullButTlsFlagOff() {
        final SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getTls()).thenReturn(SslAuthenticationMode.OFF);

        ServerConfig serverConfig = new ServerConfig(AppType.P2P, true, "somedomain:8989", CommunicationType.REST, sslConfig, null, null, "12345");
        assertThat(serverConfig.isSsl()).isFalse();
    }

    @Test
    public void tlsFlagOn() {
        final SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getTls()).thenReturn(SslAuthenticationMode.STRICT);

        ServerConfig serverConfig = new ServerConfig(AppType.P2P, true, "somedomain:8989", CommunicationType.REST, sslConfig, null, null, "12345");
        assertThat(serverConfig.isSsl()).isTrue();
    }

    @Test
    public void advertisedUrlIsDifferentToBindAddress() {
        final ServerConfig serverConfig = new ServerConfig(AppType.P2P, true, "somedomain:8989", CommunicationType.REST, null, null, "http://bindingUrl:9999", "12345");
        assertThat(serverConfig.getBindingAddress()).isEqualTo("http://bindingUrl:9999");
    }

    @Test
    public void nullAdvertisedUrlIsSameAsBindAddress() {
        final ServerConfig serverConfig = new ServerConfig(AppType.P2P, true, "somedomain:8989", CommunicationType.REST, null, null, null, "12345");
        assertThat(serverConfig.getBindingAddress()).isEqualTo("somedomain:8989");
        assertThat(serverConfig.isUnixSocket()).isFalse();
    }

    @Test
    public void unixSocketConfig() {
        final ServerConfig serverConfig = new ServerConfig(AppType.P2P, true, "unix:/bogis.ipc", CommunicationType.REST, null, null, null, "12345");
        assertThat(serverConfig.getBindingAddress()).isEqualTo("unix:/bogis.ipc");
        assertThat(serverConfig.isUnixSocket()).isTrue();
    }

    @Test
    public void ledgerId() {
        final ServerConfig serverConfig = new ServerConfig(AppType.P2P, true,"unix:/bogis.ipc", CommunicationType.REST, null, null, null, "34567");
        assertThat(serverConfig.getLedgerId()).isEqualTo("34567");
    }
}
