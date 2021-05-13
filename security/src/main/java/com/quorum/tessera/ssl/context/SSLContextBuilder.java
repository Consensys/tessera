package com.quorum.tessera.ssl.context;

import com.quorum.tessera.ssl.trust.CompositeTrustManager;
import com.quorum.tessera.ssl.trust.TrustAllManager;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import com.quorum.tessera.ssl.trust.WhiteListTrustManager;
import com.quorum.tessera.ssl.util.TlsUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.bouncycastle.operator.OperatorCreationException;

public class SSLContextBuilder {

  private static final String PROTOCOL = "TLS";

  private String address;

  private Path keyStore;

  private char[] keyStorePassword;

  private Path key;

  private Path certificate;

  private Path trustStore;

  private char[] trustStorePassword;

  private List<Path> trustedCertificates;

  private SSLContext sslContext;

  private SSLContextBuilder(
      String address,
      Path keyStore,
      char[] keyStorePassword,
      Path trustStore,
      char[] trustStorePassword)
      throws NoSuchAlgorithmException {
    this.address = address;
    this.keyStore = keyStore;
    this.keyStorePassword = keyStorePassword;
    this.trustStore = trustStore;
    this.trustStorePassword = trustStorePassword;

    this.sslContext = SSLContext.getInstance(PROTOCOL);
  }

  public static SSLContextBuilder createBuilder(
      String address,
      Path keyStore,
      char[] keyStorePassword,
      Path trustStore,
      char[] trustStorePassword)
      throws NoSuchAlgorithmException {
    return new SSLContextBuilder(
        address, keyStore, keyStorePassword, trustStore, trustStorePassword);
  }

  public SSLContextBuilder fromPemFiles(
      Path key, Path certificate, List<Path> trustedCertificates) {
    this.key = key;
    this.certificate = certificate;
    this.trustedCertificates = trustedCertificates;

    return this;
  }

  public SSLContext build() {
    return sslContext;
  }

  public SSLContextBuilder forWhiteList(Path knownHosts)
      throws GeneralSecurityException, IOException, OperatorCreationException {

    sslContext.init(
        buildKeyManagers(), new TrustManager[] {new WhiteListTrustManager(knownHosts)}, null);

    return this;
  }

  public SSLContextBuilder forCASignedCertificates()
      throws GeneralSecurityException, IOException, OperatorCreationException {

    final KeyManager[] keyManagers = buildKeyManagers();

    final TrustManager[] trustManagers = buildTrustManagers();

    sslContext.init(keyManagers, trustManagers, new SecureRandom());

    return this;
  }

  public SSLContextBuilder forAllCertificates()
      throws GeneralSecurityException, IOException, OperatorCreationException {

    sslContext.init(buildKeyManagers(), new TrustManager[] {new TrustAllManager()}, null);

    return this;
  }

  public SSLContextBuilder forTrustOnFirstUse(Path knownHostsFile)
      throws GeneralSecurityException, IOException, OperatorCreationException {

    final KeyManager[] keyManagers = buildKeyManagers();

    sslContext.init(
        keyManagers, new TrustManager[] {new TrustOnFirstUseManager(knownHostsFile)}, null);

    return this;
  }

  public SSLContextBuilder forCAOrTOFU(Path knownHostsFile)
      throws GeneralSecurityException, IOException, OperatorCreationException {

    final KeyManager[] keyManagers = buildKeyManagers();

    final TrustManager[] trustManagersFromTrustStore = buildTrustManagers();

    final int newLength = trustManagersFromTrustStore.length + 1;

    final TrustManager[] trustManagers = Arrays.copyOf(trustManagersFromTrustStore, newLength);

    trustManagers[newLength - 1] = new TrustOnFirstUseManager(knownHostsFile);

    sslContext.init(
        keyManagers,
        new TrustManager[] {new CompositeTrustManager(trustManagers)},
        new SecureRandom());

    return this;
  }

  private KeyManager[] buildKeyManagers()
      throws GeneralSecurityException, IOException, OperatorCreationException {

    if (Objects.nonNull(this.keyStore)) {
      if (!this.keyStore.toFile().exists()) {
        TlsUtils.create()
            .generateKeyStoreWithSelfSignedCertificate(
                this.address, this.keyStore, this.keyStorePassword);
      }
      return SSLKeyStoreLoader.fromJksKeyStore(this.keyStore, this.keyStorePassword);
    } else {
      return SSLKeyStoreLoader.fromPemKeyFile(this.key, this.certificate);
    }
  }

  private TrustManager[] buildTrustManagers() throws GeneralSecurityException, IOException {

    if (Objects.nonNull(this.trustStore)) {
      return SSLKeyStoreLoader.fromJksTrustStore(this.trustStore, this.trustStorePassword);
    } else {
      return SSLKeyStoreLoader.fromPemCertificatesFile(this.trustedCertificates);
    }
  }
}
