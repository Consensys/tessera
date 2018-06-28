package com.github.nexus.ssl.context;

import com.github.nexus.ssl.trust.TrustAllManager;
import com.github.nexus.ssl.trust.TrustOnFirstUseManager;
import com.github.nexus.ssl.trust.WhiteListTrustManager;
import com.github.nexus.ssl.util.TlsUtils;
import org.bouncycastle.operator.OperatorCreationException;

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

    private SSLContext sslContext;

    private SSLContextBuilder(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword) throws NoSuchAlgorithmException {
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;

        this.sslContext = SSLContext.getInstance(PROTOCOL);
    }


    public static SSLContextBuilder createBuilder(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword) throws NoSuchAlgorithmException {
        return new SSLContextBuilder(
            keyStore,
            keyStorePassword,
            trustStore,
            trustStorePassword);
    }

    public SSLContext build() {
        return sslContext;
    }


    public SSLContextBuilder forWhiteList(File knownHosts) throws NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, CertificateException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        sslContext.init(buildKeyManagers(), new TrustManager[]{new WhiteListTrustManager(knownHosts)}, null);

        return this;
    }


    public SSLContextBuilder forCASignedCertificates() throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final KeyManager[] keyManagers = buildKeyManagers();

        final TrustManager[] trustManagers = buildTrustManagers();

        sslContext.init(keyManagers, trustManagers, new SecureRandom());

        return this;
    }


    public SSLContextBuilder forAllCertificates() throws KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException {

        sslContext.init(buildKeyManagers(), new TrustManager[]{new TrustAllManager()}, null);

        return this;
    }


    public SSLContextBuilder forTrustOnFirstUse(File knownHostsFile) throws NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, CertificateException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final KeyManager[] keyManagers = buildKeyManagers();

        sslContext.init(keyManagers,
            new TrustManager[]{new TrustOnFirstUseManager(knownHostsFile)}, null);

        return this;
    }


    private KeyManager[] buildKeyManagers() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final File keyStoreFile = new File(keyStore);

        if (!keyStoreFile.exists()) {
            TlsUtils.create().generateKeyStoreWithSelfSignedCertificate(keyStoreFile, keyStorePassword);
        }

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
