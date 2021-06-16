package com.quorum.tessera.ssl.util;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public interface TlsUtils {

  String ENCRYPTION = "RSA";
  String COMMON_NAME_STRING = "CN=";
  String SIGNATURE_ALGORITHM = "SHA512WithRSAEncryption";
  String KEYSTORE_TYPE = "JKS";

  // These localhost values are added in SAN names for dev. They can be removed for production
  // if necessary
  String LOCALHOST = "localhost";
  String LOCALHOST_IP = "127.0.0.1";
  String LOCALHOST_IP_2 = "0.0.0.0";

  default void generateKeyStoreWithSelfSignedCertificate(
      String address, Path privateKeyFile, char[] password)
      throws NoSuchAlgorithmException, IOException, OperatorCreationException, CertificateException,
          InvalidKeyException, NoSuchProviderException, SignatureException, KeyStoreException {

    final SecureRandom secureRandom = new SecureRandom();

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ENCRYPTION);
    keyGen.initialize(2048, secureRandom);
    KeyPair keypair = keyGen.generateKeyPair();
    final PublicKey publicKey = keypair.getPublic();
    final PrivateKey privateKey = keypair.getPrivate();
    final String cnString = address.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)", "");
    final X500Name commonName = new X500Name(COMMON_NAME_STRING + cnString);
    Date startDate = new Date(System.currentTimeMillis());
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.YEAR, 1);
    Date endDate = calendar.getTime();

    X509v3CertificateBuilder builder =
        new JcaX509v3CertificateBuilder(
            commonName,
            new BigInteger(64, secureRandom),
            startDate,
            endDate,
            commonName,
            publicKey);

    GeneralName[] subjectAlternativeNames =
        new GeneralName[] {
          new GeneralName(GeneralName.dNSName, LOCALHOST),
          new GeneralName(GeneralName.dNSName, HostnameUtil.create().getHostName()),
          new GeneralName(GeneralName.iPAddress, LOCALHOST_IP),
          new GeneralName(GeneralName.iPAddress, LOCALHOST_IP_2),
          new GeneralName(GeneralName.iPAddress, HostnameUtil.create().getHostIpAddress())
        };

    builder.addExtension(
        Extension.subjectAlternativeName, false, new GeneralNames(subjectAlternativeNames));

    ContentSigner contentSigner =
        new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(privateKey);

    X509CertificateHolder certHolder = builder.build(contentSigner);
    X509Certificate certificate =
        new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);

    certificate.verify(publicKey);

    KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
    keyStore.load(null, null);
    keyStore.setKeyEntry("tessera", privateKey, password, new X509Certificate[] {certificate});

    try (OutputStream keyStoreFile = Files.newOutputStream(privateKeyFile)) {
      keyStore.store(keyStoreFile, password);
    }
  }

  static TlsUtils create() {
    return new TlsUtils() {};
  }
}
