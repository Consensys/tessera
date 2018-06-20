package com.github.nexus.ssl;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLContextBuilder {

    private static final String KEYSTORE_TYPE="JKS";
    private static final String PROTOCOL="TLS";

    private String keyStore;
    private String keyStorePassword;
    private String trustStore;
    private String trustStorePassword;

    private SSLContextBuilder(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword) {
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
    }

    public static SSLContextBuilder buildSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, KeyManagementException {
        return new SSLContextBuilder(
            keyStore,
            keyStorePassword,
            trustStore,
            trustStorePassword);
    }

    public SSLContext forCASignedCertificates() throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {

        final KeyManager[] keyManagers = buildKeyManagers();

        final TrustManager[] trustManagers = buildTrustManagers();

        final SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(keyManagers, trustManagers, new SecureRandom());

        return sslContext;
    }


    public SSLContext forAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {

        final SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(new KeyManager[0], new TrustManager[]{new TrustAllManager()}, null);

        return sslContext;
    }

    public SSLContext forTrustOnFirstUse(String address, File knownHostsFile) throws NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, CertificateException {
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        final KeyManager[] keyManagers = buildKeyManagers();

        sslContext.init(keyManagers,
            new TrustManager[]{new TrustOnFirstUseManager(address, knownHostsFile)}, null);

        return sslContext;
    }


    private KeyManager[] buildKeyManagers() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException {
        if (keyStore.isEmpty()) return new KeyManager[0];

        final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(new FileInputStream(this.keyStore), keyStorePassword.toCharArray());

        final KeyManagerFactory keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

        return keyManagerFactory.getKeyManagers();
    }


    private TrustManager[] buildTrustManagers() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        if (trustStore.isEmpty()) return new TrustManager[0];

        final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
        trustStore.load(new FileInputStream(this.trustStore), trustStorePassword.toCharArray());

        final TrustManagerFactory trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        return trustManagerFactory.getTrustManagers();
    }
}
