package com.github.nexus.ssl.strategy;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TrustModeTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private static TemporaryFolder dirDelegate;

    private File tmpFile;

    private File tmpKnownHosts;

    @Before
    public void setUp() {
        tmpFile = new File(tmpDir.getRoot(), "tmpFile");
        tmpKnownHosts = new File(tmpDir.getRoot(), "tmpKnownHosts");
    }

    @After
    public void after(){
        dirDelegate = tmpDir;
        assertThat(dirDelegate.getRoot().exists()).isTrue();
    }

    @AfterClass
    public static void tearDown(){
        assertThat(dirDelegate.getRoot().exists()).isFalse();
    }

    @Test
    public void testNone() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {
        assertThat(
            TrustMode.getValueIfPresent("NONE").get().createSSLContext(
                tmpFile.getPath(),
                "quorum",
                tmpFile.getPath(),
                "quorum",
                tmpKnownHosts.getPath()
            )
        ).isNotNull();
    }

    @Test
    public void testWhiteList() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {
        assertThat(
            TrustMode.getValueIfPresent("WHITELIST").get().createSSLContext(
                tmpFile.getPath(),
                "quorum",
                tmpFile.getPath(),
                "quorum",
                tmpKnownHosts.getPath()
            )
        ).isNotNull();
    }

    @Test
    public void testCA() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {
        assertThat(
            TrustMode.getValueIfPresent("CA").get().createSSLContext(
                tmpFile.getPath(),
                "quorum",
                tmpFile.getPath(),
                "quorum",
                tmpKnownHosts.getPath()
            )
        ).isNotNull();
    }

    @Test
    public void testTOFU() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {
        assertThat(
            TrustMode.getValueIfPresent("TOFU").get().createSSLContext(
                tmpFile.getPath(),
                "quorum",
                tmpFile.getPath(),
                "quorum",
                tmpKnownHosts.getPath()
            )
        ).isNotNull();
    }

    @Test
    public void testInvalidMode() {
        assertThat(TrustMode.getValueIfPresent("SOMETHING").isPresent()).isFalse();
    }
}
