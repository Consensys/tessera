package com.github.nexus.ssl.context;

import com.github.nexus.ssl.trust.TrustAllManager;
import com.github.nexus.ssl.trust.TrustOnFirstUseManager;
import com.github.nexus.ssl.trust.WhiteListTrustManager;
import com.github.nexus.ssl.util.TlsUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SSLContextBuilderTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private static TemporaryFolder dirDelegate;

    private File keyStoreFile;

    private File trustStoreFile;

    private static final String PASSWORD = "quorum";

    private SSLContextBuilder sslContextBuilder;

    File knownHostFile = mock(File.class);

    @Before
    public void setUp() throws NoSuchAlgorithmException, OperatorCreationException, InvalidKeyException, IOException, KeyStoreException, SignatureException, NoSuchProviderException, CertificateException {
        keyStoreFile = new File(tmpDir.getRoot(), "tmp-keystore");
        trustStoreFile = new File(tmpDir.getRoot(), "tmp-keystore");
        sslContextBuilder = SSLContextBuilder.createBuilder(
            keyStoreFile.getPath(),
            PASSWORD,
            trustStoreFile.getPath(),
            PASSWORD
        );
        TlsUtils.create().generateKeyStoreWithSelfSignedCertificate(keyStoreFile, PASSWORD);
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

        final File nonExistedFile = new File(tmpDir.getRoot(), "keystore");

        assertThat(nonExistedFile.exists()).isFalse();

        SSLContextBuilder otherContextBuilder = SSLContextBuilder.createBuilder(
            nonExistedFile.getPath(),"password","","");

        assertThat(otherContextBuilder.forCASignedCertificates().build()).isNotNull();

        assertThat(nonExistedFile.exists()).isTrue();

    }
}
