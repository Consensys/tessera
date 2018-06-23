package com.github.nexus.node.model;

import com.github.nexus.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;

public enum TrustMode {

    NONE {
        @Override
        public SSLContext createSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, String knownHosts) throws NoSuchAlgorithmException, KeyManagementException {
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore,trustStorePassword)
                .forAllCertificates()
                .build();
        }
    },

    WHITELIST {
        @Override
        public SSLContext createSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
            File knownHostsFile = new File(knownHosts);
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore, trustStorePassword)
                .forWhiteList(knownHostsFile)
                .build();
        }
    },

    TOFU {
        @Override
        public SSLContext createSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
            File knownHostsFile = new File(knownHosts);
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore, trustStorePassword)
                .forTrustOnFirstUse(knownHostsFile)
                .build();
        }
    },

    CA {
        @Override
        public SSLContext createSSLContext(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
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
                                                String knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException;

    public static Optional<TrustMode> getValueIfPresent(String value){
        TrustMode trustMode = null;
        try {
            trustMode = TrustMode.valueOf(value);
        }
        catch (IllegalArgumentException ex){

        }
        return Optional.ofNullable(trustMode);
    }
}
