package com.quorum.tessera.ssl.trust;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TrustAllManagerTest {

    private TrustAllManager trustAllManager = new TrustAllManager();

    @Test
    public void testCheckServerTrusted() {
        trustAllManager.checkServerTrusted(null, null);
        //Nothing to check here - allow all servers to connect
    }

    @Test
    public void testCheckClientTrusted() {
        trustAllManager.checkClientTrusted(null, null);
        //Nothing to check here - allow all clients to connect
    }

    @Test
    public void testGetAcceptedIssuers(){
        assertThat(trustAllManager.getAcceptedIssuers()).isEmpty();
    }
}
