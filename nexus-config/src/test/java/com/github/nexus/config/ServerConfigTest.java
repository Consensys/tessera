package com.github.nexus.config;

import java.net.URI;
import java.net.URISyntaxException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ServerConfigTest {

    @Test
    public void serverUri() throws URISyntaxException {
        ServerConfig config = new ServerConfig("somedomain", 8989, null);

        assertThat(config.getServerUri()).isEqualTo(new URI("somedomain:8989"));
        assertThat(config.isSsl()).isFalse();
    }

    @Test(expected = ConfigException.class)
    public void serverUriInvalidUri() throws URISyntaxException {
        new ServerConfig("&@€~:*&2", -1, null).getServerUri();
    }
}
