package com.github.nexus.ssl.strategy;

import com.github.nexus.ssl.context.SSLContextBuilder;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Optional;

public enum TrustMode {

    NONE {
        @Override
        public SSLContext createSSLContext(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword, Path knownHosts) throws NoSuchAlgorithmException, KeyManagementException, CertificateException, UnrecoverableKeyException, OperatorCreationException, IOException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore,trustStorePassword)
                .forAllCertificates()
                .build();
        }
    },

    WHITELIST {
        @Override
        public SSLContext createSSLContext(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword, Path knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore, trustStorePassword)
                .forWhiteList(knownHosts)
                .build();
        }
    },

    TOFU {
        @Override
        public SSLContext createSSLContext(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword, Path knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore, trustStorePassword)
                .forTrustOnFirstUse(knownHosts)
                .build();
        }
    },

    CA {
        @Override
        public SSLContext createSSLContext(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword, Path knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
            return SSLContextBuilder
                .createBuilder(keyStore,keyStorePassword,trustStore, trustStorePassword)
                .forCASignedCertificates()
                .build();
        }
    };

    public abstract SSLContext createSSLContext(Path keyStore,
                                                String keyStorePassword,
                                                Path trustStore,
                                                String trustStorePassword,
                                                Path knownHosts) throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException;

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
