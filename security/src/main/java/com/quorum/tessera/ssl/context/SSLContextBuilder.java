package com.quorum.tessera.ssl.context;

import com.quorum.tessera.ssl.trust.TrustAllManager;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import com.quorum.tessera.ssl.trust.WhiteListTrustManager;
import com.quorum.tessera.ssl.util.TlsUtils;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLContextBuilder {

    private static final String KEYSTORE_TYPE="JKS";
    private static final String PROTOCOL="TLS";

    private Path keyStore;
    private String keyStorePassword;
    private Path trustStore;
    private String trustStorePassword;

    private SSLContext sslContext;

    private SSLContextBuilder(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword) throws NoSuchAlgorithmException {
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;

        this.sslContext = SSLContext.getInstance(PROTOCOL);
    }


    public static SSLContextBuilder createBuilder(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword) throws NoSuchAlgorithmException {
        return new SSLContextBuilder(
            keyStore,
            keyStorePassword,
            trustStore,
            trustStorePassword);
    }

    public SSLContext build() {
        return sslContext;
    }


    public SSLContextBuilder forWhiteList(Path knownHosts) throws NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, CertificateException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

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


    public SSLContextBuilder forTrustOnFirstUse(Path knownHostsFile) throws NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, CertificateException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final KeyManager[] keyManagers = buildKeyManagers();

        sslContext.init(keyManagers,
            new TrustManager[]{new TrustOnFirstUseManager(knownHostsFile)}, null);

        return this;
    }


    private KeyManager[] buildKeyManagers() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        if (Files.notExists(keyStore)) {
            TlsUtils.create().generateKeyStoreWithSelfSignedCertificate(keyStore, keyStorePassword);
        }

        final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

        try (final InputStream in = Files.newInputStream(this.keyStore)) {
            keyStore.load(in, keyStorePassword.toCharArray());
        }

        final KeyManagerFactory keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

        return keyManagerFactory.getKeyManagers();
    }


    private TrustManager[] buildTrustManagers() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);

        try (final InputStream in = Files.newInputStream(this.trustStore)) {
            trustStore.load(in, trustStorePassword.toCharArray());
        }

        final TrustManagerFactory trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        return trustManagerFactory.getTrustManagers();
    }

}
