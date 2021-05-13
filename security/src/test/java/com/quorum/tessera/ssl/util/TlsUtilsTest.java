package com.quorum.tessera.ssl.util;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;

public class TlsUtilsTest {

  private static final char[] PASSWORD = "quorum".toCharArray();

  private static final String ALIAS = "tessera";

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Test
  public void testGenerateKeys()
      throws OperatorCreationException, InvalidKeyException, NoSuchAlgorithmException, IOException,
          SignatureException, NoSuchProviderException, CertificateException, KeyStoreException {

    final Path privateKeyFile = Files.createTempFile("privatekey", ".tmp");
    Files.deleteIfExists(privateKeyFile);

    assertThat(Files.exists(privateKeyFile)).isFalse();

    TlsUtils.create()
        .generateKeyStoreWithSelfSignedCertificate(
            "https://localhost:8080", privateKeyFile, PASSWORD);

    assertThat(Files.exists(privateKeyFile)).isTrue();

    // Read keystore from created file
    final KeyStore keyStore = KeyStore.getInstance("JKS");

    try (InputStream in = Files.newInputStream(privateKeyFile)) {
      keyStore.load(in, PASSWORD);
    }

    assertThat(keyStore.containsAlias(ALIAS)).isTrue();

    Certificate certificate = keyStore.getCertificate(ALIAS);

    assertThat(certificate).isNotNull();
    assertThat(certificate).isInstanceOf(X509Certificate.class);
    assertThat(((X509Certificate) certificate))
        .extracting("info")
        .isNotNull()
        .extracting("issuer")
        .isNotNull()
        .extracting("names")
        .isNotNull();
    //                .size()
    //                .isEqualTo(1);
  }
}
