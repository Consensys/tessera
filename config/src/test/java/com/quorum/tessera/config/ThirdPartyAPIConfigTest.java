package com.quorum.tessera.config;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class ThirdPartyAPIConfigTest {

    ThirdPartyAPIConfig thirdPartyAPIConfig = new ThirdPartyAPIConfig(true, "http://localhost", 1234, null);

    @Test
    public void isEnabled() {
        assertEquals(true, thirdPartyAPIConfig.isEnabled());
        assertFalse(thirdPartyAPIConfig.isSsl());
    }

    @Test
    public void getHostName() {
        assertEquals("http://localhost", thirdPartyAPIConfig.getHostName());
    }

    @Test
    public void getPort() {
        assertEquals(new Integer(1234), thirdPartyAPIConfig.getPort());
    }

    @Test
    public void getSslConfig() {
        assertNull(thirdPartyAPIConfig.getSslConfig());
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.OFF, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        ThirdPartyAPIConfig config = new ThirdPartyAPIConfig(true, "http://localhost", 1234, sslConfig);
        assertFalse(config.isSsl());
        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        config = new ThirdPartyAPIConfig(true, "http://localhost", 1234, sslConfig);
        assertTrue(config.isSsl());
    }

    @Test(expected = ConfigException.class)
    public void getServerUri() throws URISyntaxException {
        assertEquals(new URI("http://localhost:1234"), thirdPartyAPIConfig.getServerUri());
        ThirdPartyAPIConfig config = new ThirdPartyAPIConfig(true, "&@€~:*&2", 1234, null);
        config.getServerUri();
    }
}
