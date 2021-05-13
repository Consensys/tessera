package com.quorum.tessera.ssl.context.model;

import java.nio.file.Path;
import java.util.List;

public class SSLContextProperties {

  private String address;

  private Path keyStore;

  private char[] keyStorePassword;

  private Path key;

  private Path certificate;

  private Path trustStore;

  private char[] trustStorePassword;

  private List<Path> trustedCertificates;

  private Path knownHosts;

  public SSLContextProperties(
      String address,
      Path keyStore,
      char[] keyStorePassword,
      Path key,
      Path certificate,
      Path trustStore,
      char[] trustStorePassword,
      List<Path> trustedCertificates,
      Path knownHosts) {
    this.address = address;
    this.keyStore = keyStore;
    this.keyStorePassword = keyStorePassword;
    this.key = key;
    this.certificate = certificate;
    this.trustStore = trustStore;
    this.trustStorePassword = trustStorePassword;
    this.trustedCertificates = trustedCertificates;
    this.knownHosts = knownHosts;
  }

  public String getAddress() {
    return address;
  }

  public Path getKeyStore() {
    return keyStore;
  }

  public char[] getKeyStorePassword() {
    return keyStorePassword;
  }

  public Path getKey() {
    return key;
  }

  public Path getCertificate() {
    return certificate;
  }

  public Path getTrustStore() {
    return trustStore;
  }

  public char[] getTrustStorePassword() {
    return trustStorePassword;
  }

  public List<Path> getTrustedCertificates() {
    return trustedCertificates;
  }

  public Path getKnownHosts() {
    return knownHosts;
  }
}
