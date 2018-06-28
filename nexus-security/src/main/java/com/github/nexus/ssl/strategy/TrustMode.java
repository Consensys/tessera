package com.github.nexus.ssl.strategy;

import com.github.nexus.ssl.context.SSLContextBuilder;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Optional;

public enum TrustMode {

    NONE {
        @Override
        public SSLContext createSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, CertificateException, UnrecoverableKeyException, OperatorCreationException, IOException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore,trustStorePassword)
                .forAllCertificates()
                .build();
        }
    },

    WHITELIST {
        @Override
        public SSLContext createSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
            final File knownHostsFile = new File(knownHosts);
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore, trustStorePassword)
                .forWhiteList(knownHostsFile)
                .build();
        }
    },

    TOFU {
        @Override
        public SSLContext createSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
            final File knownHostsFile = new File(knownHosts);
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore, trustStorePassword)
                .forTrustOnFirstUse(knownHostsFile)
                .build();
        }
    },

    CA {
        @Override
        public SSLContext createSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore, trustStorePassword)
                .forCASignedCertificates()
                .build();
        }
    };

    public abstract SSLContext createSSLContext(String keyStore,
                                                String keyStorePassword,
                                                String trustStore,
                                                String trustStorePassword,
                                                String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException;

    public static Optional<TrustMode> getValueIfPresent(String value){
        TrustMode trustMode = null;
        try {
            trustMode = TrustMode.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex){

        }
        return Optional.ofNullable(trustMode);
    }
}
