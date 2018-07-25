package com.quorum.tessera.ssl.context;

import com.quorum.tessera.ssl.trust.TrustAllManager;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import com.quorum.tessera.ssl.trust.WhiteListTrustManager;
import com.quorum.tessera.ssl.util.TlsUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;

public class SSLContextBuilderTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private Path keyStoreFile;

    private Path knownHostFile;


    private static final String PASSWORD = "quorum";

    private SSLContextBuilder sslContextBuilder;

    @Before
    public void setUp() throws NoSuchAlgorithmException, OperatorCreationException, InvalidKeyException, IOException, KeyStoreException, SignatureException, NoSuchProviderException, CertificateException {
        keyStoreFile = Paths.get(tmpDir.getRoot().getPath(), "keystore");
        knownHostFile = Paths.get(tmpDir.getRoot().getPath(), "knownHosts");
        sslContextBuilder = SSLContextBuilder.createBuilder(
            keyStoreFile,
            PASSWORD,
            keyStoreFile,
            PASSWORD
        );
        TlsUtils.create().generateKeyStoreWithSelfSignedCertificate(keyStoreFile, PASSWORD);
    }

    @Test
    public void testBuildForTrustOnFirstUse() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final SSLContext sslContext = sslContextBuilder.forTrustOnFirstUse(knownHostFile).build();

        assertThat(sslContext).isNotNull()
            .extracting("contextSpi").isNotNull()
            .extracting("trustManager").isNotNull()
            .extracting("tm").isNotNull()
            .hasAtLeastOneElementOfType(TrustOnFirstUseManager.class);

    }

    @Test
    public void testBuildForWhiteList() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final SSLContext sslContext = sslContextBuilder.forWhiteList(knownHostFile).build();

        assertThat(sslContext).isNotNull()
            .extracting("contextSpi").isNotNull()
            .extracting("trustManager").isNotNull()
            .extracting("tm").isNotNull()
            .hasAtLeastOneElementOfType(WhiteListTrustManager.class);


    }

    @Test
    public void testBuildForCASignedCertificates() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final SSLContext sslContext = sslContextBuilder.forCASignedCertificates().build();

        assertThat(sslContext).isNotNull()
            .extracting("contextSpi").isNotNull()
            .extracting("trustManager").isNotNull()
            .extracting("trustedCerts").isNotNull()
            .hasSize(1);

        assertThat(sslContext)
            .extracting("contextSpi")
            .extracting("keyManager").isNotNull()
            .extracting("credentialsMap").isNotNull()
            .hasSize(1);

    }

    @Test
    public void testBuildForAllCertificates() throws KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, SignatureException, OperatorCreationException, NoSuchProviderException {

        final SSLContext sslContext = sslContextBuilder.forAllCertificates().build();

        assertThat(sslContext).isNotNull()
            .extracting("contextSpi").isNotNull()
            .extracting("trustManager").isNotNull()
            .extracting("tm").isNotNull()
            .hasAtLeastOneElementOfType(TrustAllManager.class);
    }

    @Test
    public void testKeyStoreNotExistedThenGenerated() throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final Path nonExistedFile = Paths.get(tmpDir.getRoot().getPath(), "somefile");

        assertThat(Files.exists(nonExistedFile)).isFalse();

        SSLContextBuilder otherContextBuilder = SSLContextBuilder.createBuilder(
            nonExistedFile, "password", keyStoreFile, PASSWORD);

        assertThat(otherContextBuilder.forCASignedCertificates().build()).isNotNull();

        assertThat(Files.exists(nonExistedFile)).isTrue();

        Files.deleteIfExists(nonExistedFile);

    }
}
