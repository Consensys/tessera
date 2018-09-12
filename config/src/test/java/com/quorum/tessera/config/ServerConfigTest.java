package com.quorum.tessera.config;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerConfigTest {

    @Test
    public void serverUri() throws URISyntaxException {
        ServerConfig config = new ServerConfig("somedomain", 8989, 50521, null, null, null, null);

        assertThat(config.getServerUri()).isEqualTo(new URI("somedomain:8989"));
        assertThat(config.isSsl()).isFalse();
    }

    @Test
    public void bindingUri() throws URISyntaxException {
        ServerConfig config = new ServerConfig("somedomain", 8989, 50521,  null, null, null, "http://somedomain:9000");
        assertThat(config.getBindingUri()).isEqualTo(new URI("http://somedomain:9000"));
        assertThat(config.isSsl()).isFalse();
    }

    @Test
    public void grpcUri() throws URISyntaxException {
        ServerConfig config = new ServerConfig("somedomain", 8989, 50521,  null, null, null, "http://somedomain:9000");
        assertThat(config.getGrpcUri()).isEqualTo(new URI("somedomain:50521"));
    }

    @Test(expected = ConfigException.class)
    public void serverUriInvalidUri() {
        new ServerConfig("&@€~:*&2", -1, 50521, null,null, null, null).getServerUri();
    }

    @Test(expected = ConfigException.class)
    public void bindingUriInvalidUri() {
        new ServerConfig("&@€~:*&2", -1, 50521, null,null, null, "&@€~:*&2").getBindingUri();
    }

    @Test(expected = ConfigException.class)
    public void grpcUriInvalidUri() {
        new ServerConfig("&@€~:*&2", -1, 50521, null,null, null, "&@€~:*&2").getGrpcUri();
    }

    @Test
    public void sslNotNullButTlsFlagOff() {
        final SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.OFF, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        ServerConfig serverConfig = new ServerConfig("somedomain", 8989, 50521, null, sslConfig, null, null);
        assertThat(serverConfig.isSsl()).isFalse();
    }

    @Test
    public void tlsFlagOn() {
        final SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        ServerConfig serverConfig = new ServerConfig("somedomain", 8989, 50521, null, sslConfig, null, null);
        assertThat(serverConfig.isSsl()).isTrue();
    }

    @Test
    public void advertisedUrlIsDifferentToBindAddress() {
        final ServerConfig serverConfig = new ServerConfig("somedomain", 8989, 50521, null, null, null, "http://bindingUrl:9999");
        assertThat(serverConfig.getBindingAddress()).isEqualTo("http://bindingUrl:9999");
    }

    @Test
    public void nullAdvertisedUrlIsSameAsBindAddress() {
        final ServerConfig serverConfig = new ServerConfig("somedomain", 8989, 50521,null, null, null, null);
        assertThat(serverConfig.getBindingAddress()).isEqualTo("somedomain:8989");
    }
    
    
    @Test(expected = ConfigException.class)
    public void invalidGrpcUri() {
        final ServerConfig serverConfig = new ServerConfig("^$1%&@*(@)", 8989, 50521,null, null, null, null);
        serverConfig.getGrpcUri();
        
        
    }
}
