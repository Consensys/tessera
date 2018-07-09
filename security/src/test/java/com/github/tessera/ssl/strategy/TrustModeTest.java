package com.github.tessera.ssl.strategy;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TrustModeTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private Path tmpFile;

    private Path tmpKnownHosts;

    @Before
    public void setUp() {
        tmpFile = Paths.get(tmpDir.getRoot().getPath(), "tmpFile");
        tmpKnownHosts = Paths.get(tmpDir.getRoot().getPath(),"knownHosts");
    }


    @Test
    public void testNone() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {

        assertThat(
            TrustMode.NONE.createSSLContext(
                tmpFile,
                "quorum",
                tmpFile,
                "quorum",
                tmpKnownHosts
            )
        ).isNotNull();

    }

    @Test
    public void testWhiteList() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {
        assertThat(
            TrustMode.getValueIfPresent("WHITELIST").get().createSSLContext(
                tmpFile,
                "quorum",
                tmpFile,
                "quorum",
                tmpKnownHosts
            )
        ).isNotNull();
    }

    @Test
    public void testCA() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {
        assertThat(
            TrustMode.getValueIfPresent("CA").get().createSSLContext(
                tmpFile,
                "quorum",
                tmpFile,
                "quorum",
                tmpKnownHosts
            )
        ).isNotNull();
    }

    @Test
    public void testTOFU() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {
        assertThat(
            TrustMode.getValueIfPresent("TOFU").get().createSSLContext(
                tmpFile,
                "quorum",
                tmpFile,
                "quorum",
                tmpKnownHosts
            )
        ).isNotNull();
    }

    @Test
    public void testInvalidMode() {
        assertThat(TrustMode.getValueIfPresent("SOMETHING").isPresent()).isFalse();
    }
}
