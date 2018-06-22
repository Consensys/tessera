package com.github.nexus.ssl;

import org.junit.Test;

import java.security.cert.CertificateException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TrustAllManagerTest {

    TrustAllManager trustAllManager = new TrustAllManager();

    @Test
    public void testCheckServerTrusted() throws CertificateException {
        trustAllManager.checkServerTrusted(null, null);
        //Nothing to check here - allow all servers to connect
    }

    @Test
    public void testCheckClientTrusted() throws CertificateException {
        trustAllManager.checkClientTrusted(null, null);
        //Nothing to check here - allow all clients to connect
    }

    @Test
    public void testGetAcceptedIssuers(){
        assertThat(trustAllManager.getAcceptedIssuers()).isEmpty();
    }
}
