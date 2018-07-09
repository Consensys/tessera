package com.github.tessera.ssl.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

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

public interface TlsUtils {


    String ENCRYPTION = "RSA";
    String COMMON_NAME_STRING = "CN=";
    String SIGNATURE_ALGORITHM = "SHA512WithRSAEncryption";
    String KEYSTORE_TYPE = "JKS";
    String DEFAULT_HOSTNAME = "localhost";

    Provider provider = new BouncyCastleProvider();
    HostnameUtil HOSTNAME_UTIL = HostnameUtil.create();

    default void generateKeyStoreWithSelfSignedCertificate(Path privateKeyFile, String password)
        throws NoSuchAlgorithmException, IOException, OperatorCreationException,
        CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException, KeyStoreException {

        final SecureRandom secureRandom = new SecureRandom();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ENCRYPTION);
        keyGen.initialize(2048, secureRandom);
        KeyPair keypair = keyGen.generateKeyPair();
        PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate();

        final X500Name commonName = new X500Name(COMMON_NAME_STRING + DEFAULT_HOSTNAME);
        Date startDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1);
        Date endDate = calendar.getTime();

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
            commonName,
            new BigInteger(64, secureRandom),
            startDate,
            endDate,
            commonName,
            publicKey);

        ContentSigner contentSigner = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(privateKey);
        X509CertificateHolder certHolder = builder.build(contentSigner) ;
        X509Certificate certificate =
            new JcaX509CertificateConverter().setProvider(provider).getCertificate(certHolder);

        certificate.verify(publicKey);

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);
        keyStore.setKeyEntry("tessera",privateKey, password.toCharArray(), new X509Certificate[]{certificate});

        try(final OutputStream keyStoreFile = Files.newOutputStream(privateKeyFile)) {
            keyStore.store(keyStoreFile, password.toCharArray());
        }
    }

    static TlsUtils create(){
        Security.addProvider(new BouncyCastleProvider());
        return new TlsUtils() {
        };
    }


}
