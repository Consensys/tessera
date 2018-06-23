package com.github.nexus.ssl.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

public class TlsUtils {

    private static final String ENCRYPTION = "RSA";
    private static final String COMMON_NAME_STRING = "CN=";
    private static final String SIGNATURE_ALGORITHM = "SHA512WithRSAEncryption";
    private static final String KEYSTORE_TYPE = "JKS";
    private static final char[] DEFAULT_PASSWORD = "password".toCharArray();

    private static final Provider provider = new BouncyCastleProvider();
    private static final HostnameUtil HOST_NAME_UTIL;

    static
    {
        Security.addProvider(provider);
        HOST_NAME_UTIL = HostnameUtil.create();
    }

    public void generateKeyStoreWithSelfSignedCertificate(File privateKeyFile, File certificateFile)
        throws NoSuchAlgorithmException, IOException, OperatorCreationException,
        CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException, KeyStoreException {

        SecureRandom secureRandom = new SecureRandom();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ENCRYPTION);
        keyGen.initialize(2048, secureRandom);
        KeyPair keypair = keyGen.generateKeyPair();
        PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate();

        X500Name commonName = new X500Name(COMMON_NAME_STRING + HOST_NAME_UTIL.getHostName());
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
        keyStore.setKeyEntry("nexus",privateKey, DEFAULT_PASSWORD, new X509Certificate[]{certificate});

        FileOutputStream keyStoreFile = new FileOutputStream(privateKeyFile);

        keyStore.store(keyStoreFile, DEFAULT_PASSWORD);
        keyStoreFile.close();

    }

}
