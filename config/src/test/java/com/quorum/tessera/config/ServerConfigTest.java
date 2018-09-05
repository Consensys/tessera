package com.quorum.tessera.config;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerConfigTest {

    @Test
    public void serverUri() throws URISyntaxException {
        final ServerConfig config = new ServerConfig("somedomain", 8989, null, null, null);

        assertThat(config.getServerUri()).isEqualTo(new URI("somedomain:8989"));
        assertThat(config.isSsl()).isFalse();
    }

    @Test(expected = ConfigException.class)
    public void serverUriInvalidUri() {
        new ServerConfig("&@€~:*&2", -1, null, null, null).getServerUri();
    }

    @Test
    public void sslNotNullButTlsFlagOff() {
        final SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.OFF, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        final ServerConfig serverConfig = new ServerConfig("somedomain", 8989, sslConfig, null, null);
        assertThat(serverConfig.isSsl()).isFalse();
    }

    @Test
    public void tlsFlagOn() {
        final SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        final ServerConfig serverConfig = new ServerConfig("somedomain", 8989, sslConfig, null, null);
        assertThat(serverConfig.isSsl()).isTrue();
    }

    @Test
    public void advertisedUrlIsDifferentToBindAddress() {
        final ServerConfig serverConfig = new ServerConfig("somedomain", 8989, null, null, "http://bindingUrl:9999");
        assertThat(serverConfig.getBindingAddress()).isEqualTo("http://bindingUrl:9999");
    }

    @Test
    public void nullAdvertisedUrlIsSameAsBindAddress() {
        final ServerConfig serverConfig = new ServerConfig("somedomain", 8989, null, null, null);
        assertThat(serverConfig.getBindingAddress()).isEqualTo("somedomain:8989");
    }

}
