package com.github.nexus.ssl;

import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;

public class SSLContextBuilderTest {

    private SSLContextBuilder sslContextBuilder;

    @Before
    public void init() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        sslContextBuilder = SSLContextBuilder.buildSSLContext(
            "/Users/namtruong/workspace/Nexus/nexus-security/src/test/resources/server1-keystore",
            "quorum",
            "/Users/namtruong/workspace/Nexus/nexus-security/src/test/resources/server-truststore",
            "quorum");
    }

    @Test
    public void testBuildSSLContextForCASignedCertificates() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {

        SSLContext sslContext = sslContextBuilder.forCASignedCertificates();
        assertThat(sslContext).isNotNull();

    }

    @Test
    public void testBuildSSLContextForAllCertificates() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = sslContextBuilder.forAllCertificates();
        assertThat(sslContext).isNotNull();

    }
}
