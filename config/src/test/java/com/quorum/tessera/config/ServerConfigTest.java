package com.quorum.tessera.config;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerConfigTest {

    @Test
    public void serverUri() throws URISyntaxException {
        ServerConfig config = new ServerConfig("somedomain", 8989, null, null);

        assertThat(config.getServerUri()).isEqualTo(new URI("somedomain:8989"));
        assertThat(config.isSsl()).isFalse();
    }

    @Test(expected = ConfigException.class)
    public void serverUriInvalidUri() {
        new ServerConfig("&@€~:*&2", -1, null, null).getServerUri();
    }

    @Test
    public void sslNotNullButTlsFlagOff() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.OFF, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null
        );
        ServerConfig serverConfig = new ServerConfig("somedomain", 8989, sslConfig, null);
        assertThat(serverConfig.isSsl()).isFalse();
    }

    @Test
    public void tlsFlagOn() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null
        );
        ServerConfig serverConfig = new ServerConfig("somedomain", 8989, sslConfig, null);
        assertThat(serverConfig.isSsl()).isTrue();
    }

}
