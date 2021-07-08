package com.quorum.tessera.ssl.context;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

final class SSLKeyStoreLoader {

  private static final Pattern PRIVATE_KEY_PATTERN =
      Pattern.compile(
          "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+"
              + "([a-z0-9+/=\\r\\n]+)"
              + "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+",
          2);

  private static final Pattern CERTIFICATE_PATTERN =
      Pattern.compile(
          "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+"
              + "([a-z0-9+/=\\r\\n]+)"
              + "-+END\\s+.*CERTIFICATE[^-]*-+",
          2);

  private static final String KEYSTORE_TYPE = "JKS";

  private static final String ALIAS = "tessera-node";

  private static final char[] EMPTY_PASSWORD = "".toCharArray();

  private static final Base64.Decoder decoder = Base64.getMimeDecoder();

  private SSLKeyStoreLoader() {}

  static KeyManager[] fromJksKeyStore(Path keyStoreFile, char[] keyStorePassword)
      throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException,
          UnrecoverableKeyException {

    final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

    try (InputStream in = Files.newInputStream(keyStoreFile)) {
      keyStore.load(in, keyStorePassword);
    }

    final KeyManagerFactory keyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, keyStorePassword);

    return keyManagerFactory.getKeyManagers();
  }

  static KeyManager[] fromPemKeyFile(Path key, Path certificate)
      throws IOException, GeneralSecurityException {

    final PKCS8EncodedKeySpec encodedKeySpec = getEncodedKeySpec(key);

    final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    final PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);

    final List<X509Certificate> certificates = getCertificates(certificate);

    KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
    keyStore.load(null, null);
    keyStore.setKeyEntry(
        ALIAS, privateKey, EMPTY_PASSWORD, certificates.stream().toArray(Certificate[]::new));

    final KeyManagerFactory keyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, EMPTY_PASSWORD);

    return keyManagerFactory.getKeyManagers();
  }

  static TrustManager[] fromJksTrustStore(Path trustStoreFile, char[] trustStorePassword)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
    final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);

    try (InputStream in = Files.newInputStream(trustStoreFile)) {
      trustStore.load(in, trustStorePassword);
    }

    final TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(trustStore);

    return trustManagerFactory.getTrustManagers();
  }

  static TrustManager[] fromPemCertificatesFile(List<Path> trustedCertificates)
      throws GeneralSecurityException, IOException {
    final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
    trustStore.load(null, null);

    List<X509Certificate> certificates = new ArrayList<>();

    for (Path path : trustedCertificates) {
      certificates.addAll(getCertificates(path));
    }

    for (X509Certificate certificate : certificates) {
      X500Principal principal = certificate.getSubjectX500Principal();
      trustStore.setCertificateEntry(principal.getName("RFC2253"), certificate);
    }

    final TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(trustStore);

    return trustManagerFactory.getTrustManagers();
  }

  private static PKCS8EncodedKeySpec getEncodedKeySpec(Path keyFile)
      throws IOException, GeneralSecurityException {

    final String keyFileContent = readPemFile(keyFile);

    final Matcher matcher = PRIVATE_KEY_PATTERN.matcher(keyFileContent);

    if (!matcher.find()) {
      throw new KeyStoreException("NO PRIVATE KEY FOUND IN FILE " + keyFile);
    }

    final byte[] encodedKey = decoder.decode(matcher.group(1));

    return new PKCS8EncodedKeySpec(encodedKey);
  }

  private static List<X509Certificate> getCertificates(Path certificateFile)
      throws IOException, GeneralSecurityException {

    final String certFileContent = readPemFile(certificateFile);

    final Matcher matcher = CERTIFICATE_PATTERN.matcher(certFileContent);

    List<X509Certificate> certificates = new ArrayList<>();

    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

    int start = 0;
    while (matcher.find(start)) {
      byte[] buffer = decoder.decode(matcher.group(1));
      final X509Certificate certificate =
          (X509Certificate)
              certificateFactory.generateCertificate(new ByteArrayInputStream(buffer));
      certificates.add(certificate);
      start = matcher.end();
    }

    if (certificates.isEmpty()) {
      throw new CertificateException("NO CERTIFICATE FOUND IN FILE");
    }

    return certificates;
  }

  private static String readPemFile(Path file) throws IOException {

    try (BufferedReader reader = Files.newBufferedReader(file)) {

      StringBuilder fileContent = new StringBuilder();
      CharBuffer charBuffer = CharBuffer.allocate(4096);

      while (reader.read(charBuffer) != -1) {
        charBuffer.flip();
        fileContent.append(charBuffer);
        charBuffer.clear();
      }
      return fileContent.toString();
    }
  }
}
