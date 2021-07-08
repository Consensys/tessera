package com.quorum.tessera.ssl.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.ssl.trust.CompositeTrustManager;
import com.quorum.tessera.ssl.util.TlsUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SSLContextBuilderTest {

  @Rule public TemporaryFolder tmpDir = new TemporaryFolder();

  private Path keyStoreFile;

  private Path knownHostFile;

  private Path key;

  private Path certificate;

  private List<Path> trustedCertificates;

  private static final char[] PASSWORD = "quorum".toCharArray();

  private static final String LOCALHOST = "localhost";

  private SSLContextBuilder sslContextBuilder;

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Before
  public void setUp()
      throws NoSuchAlgorithmException, OperatorCreationException, InvalidKeyException, IOException,
          KeyStoreException, SignatureException, NoSuchProviderException, CertificateException,
          URISyntaxException {
    keyStoreFile = Paths.get(tmpDir.getRoot().getPath(), "keystore");
    knownHostFile = Paths.get(tmpDir.getRoot().getPath(), "knownHosts");
    key = Paths.get(getClass().getResource("/key.pem").toURI());
    certificate = Paths.get(getClass().getResource("/cert.pem").toURI());
    trustedCertificates = Arrays.asList(certificate);
    sslContextBuilder =
        SSLContextBuilder.createBuilder(LOCALHOST, keyStoreFile, PASSWORD, keyStoreFile, PASSWORD);
    TlsUtils.create().generateKeyStoreWithSelfSignedCertificate(LOCALHOST, keyStoreFile, PASSWORD);
  }

  @Test
  public void testBuildForTrustOnFirstUse()
      throws GeneralSecurityException, IOException, OperatorCreationException {

    final SSLContext sslContext = sslContextBuilder.forTrustOnFirstUse(knownHostFile).build();

    assertThat(sslContext)
        .isNotNull()
        .extracting("contextSpi")
        .isNotNull()
        .extracting("trustManager")
        .isNotNull()
        .extracting("tm")
        .isNotNull();
    // .hasAtLeastOneElementOfType(TrustOnFirstUseManager.class);
  }

  @Test
  public void testBuildForWhiteList()
      throws GeneralSecurityException, IOException, OperatorCreationException {

    final SSLContext sslContext = sslContextBuilder.forWhiteList(knownHostFile).build();

    assertThat(sslContext)
        .isNotNull()
        .extracting("contextSpi")
        .isNotNull()
        .extracting("trustManager")
        .isNotNull()
        .extracting("tm")
        .isNotNull();
    // .hasAtLeastOneElementOfType(WhiteListTrustManager.class);
  }

  @Test
  public void testBuildForCASignedCertificates()
      throws GeneralSecurityException, IOException, OperatorCreationException {

    final SSLContext sslContext = sslContextBuilder.forCASignedCertificates().build();

    assertThat(sslContext)
        .isNotNull()
        .extracting("contextSpi")
        .isNotNull()
        .extracting("trustManager")
        .isNotNull()
        .extracting("trustedCerts")
        .isNotNull();
    //  .hasSize(1);

    assertThat(sslContext)
        .extracting("contextSpi")
        .extracting("keyManager")
        .isNotNull()
        .extracting("credentialsMap")
        .isNotNull();
    //  .hasSize(1);
  }

  @Test
  public void testBuildForAllCertificates()
      throws GeneralSecurityException, IOException, OperatorCreationException {

    final SSLContext sslContext = sslContextBuilder.forAllCertificates().build();

    assertThat(sslContext)
        .isNotNull()
        .extracting("contextSpi")
        .isNotNull()
        .extracting("trustManager")
        .isNotNull()
        .extracting("tm")
        .isNotNull();
    // .hasAtLeastOneElementOfType(TrustAllManager.class);
  }

  @Test
  public void testBuildForCAOrTOFU()
      throws GeneralSecurityException, IOException, OperatorCreationException {
    final SSLContext sslContext = sslContextBuilder.forCAOrTOFU(knownHostFile).build();

    assertThat(sslContext)
        .isNotNull()
        .extracting("contextSpi")
        .isNotNull()
        .extracting("trustManager")
        .isNotNull()
        .extracting("tm")
        .isNotNull()
        //  .first()
        .isInstanceOf(CompositeTrustManager.class)
        .extracting("trustManagers")
        .isNotNull();
  }

  @Test
  public void testKeyStoreNotExistedThenGenerated()
      throws GeneralSecurityException, IOException, OperatorCreationException {

    final Path nonExistedFile = Paths.get(tmpDir.getRoot().getPath(), "somefile");

    assertThat(Files.exists(nonExistedFile)).isFalse();

    SSLContextBuilder otherContextBuilder =
        SSLContextBuilder.createBuilder(
            LOCALHOST, nonExistedFile, "password".toCharArray(), keyStoreFile, PASSWORD);

    assertThat(otherContextBuilder.forCASignedCertificates().build()).isNotNull();

    assertThat(Files.exists(nonExistedFile)).isTrue();
  }

  @Test
  public void testBuildUsingPemFiles()
      throws IOException, GeneralSecurityException, OperatorCreationException {

    SSLContext context =
        SSLContextBuilder.createBuilder(LOCALHOST, null, null, null, null)
            .fromPemFiles(key, certificate, trustedCertificates)
            .forCASignedCertificates()
            .build();

    assertThat(context)
        .isNotNull()
        .extracting("contextSpi")
        .isNotNull()
        .extracting("trustManager")
        .isNotNull()
        .extracting("trustedCerts")
        .isNotNull();
    // .hasSize(1);

    assertThat(context)
        .extracting("contextSpi")
        .extracting("keyManager")
        .isNotNull()
        .extracting("credentialsMap")
        .isNotNull();
    //  .hasSize(1);
  }
}
