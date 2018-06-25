
package com.github.nexus.socket;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class HttpProxyFactoryTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void testcreateSecureHttpProxy() throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {

        final URI uri = new URI("http://bogus.com");

        File tmpFile = new File(tmpDir.getRoot(), "keystores");

        final HttpProxy httpProxy = new HttpProxyFactory(uri)
            .auth("strict")
            .keyStore(tmpFile.getPath())
            .keyStorePassword("somepwd")
            .trustStore(tmpFile.getPath())
            .trustStorePassword("somepwd")
            .knownServers(tmpFile.getPath())
            .trustMode("NONE")
            .create();

        assertThat(httpProxy).isNotNull();

    }

    @Test
    public void testInsecureHttpProxy() throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {
        final URI uri = new URI("http://bogus.com");

        File tmpFile = new File(tmpDir.getRoot(), "keystores");

        final HttpProxy httpProxy = new HttpProxyFactory(uri)
            .auth("off")
            .keyStore(tmpFile.getPath())
            .keyStorePassword("somepwd")
            .trustStore(tmpFile.getPath())
            .trustStorePassword("somepwd")
            .knownServers(tmpFile.getPath())
            .trustMode("NONE")
            .create();

        assertThat(httpProxy).isNotNull();
    }

}
