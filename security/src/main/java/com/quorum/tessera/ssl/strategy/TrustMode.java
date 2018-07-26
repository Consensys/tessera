package com.quorum.tessera.ssl.strategy;

import com.quorum.tessera.ssl.context.SSLContextBuilder;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Optional;

public enum TrustMode {

    NONE {
        @Override
        public SSLContext createSSLContext(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword, Path knownHosts) throws GeneralSecurityException, OperatorCreationException, IOException {
            return SSLContextBuilder
                .createBuilder(keyStore, keyStorePassword, trustStore, trustStorePassword)
                .forAllCertificates()
                .build();
        }
    },

    WHITELIST {
        @Override
        public SSLContext createSSLContext(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword, Path knownHosts) throws GeneralSecurityException, IOException, OperatorCreationException {
            return SSLContextBuilder
                .createBuilder(keyStore, keyStorePassword, trustStore, trustStorePassword)
                .forWhiteList(knownHosts)
                .build();
        }
    },

    TOFU {
        @Override
        public SSLContext createSSLContext(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword, Path knownHosts) throws GeneralSecurityException, IOException, OperatorCreationException {
            return SSLContextBuilder
                .createBuilder(keyStore, keyStorePassword, trustStore, trustStorePassword)
                .forTrustOnFirstUse(knownHosts)
                .build();
        }
    },

    CA {
        @Override
        public SSLContext createSSLContext(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword, Path knownHosts) throws GeneralSecurityException, IOException, OperatorCreationException {
            return SSLContextBuilder
                .createBuilder(keyStore, keyStorePassword, trustStore, trustStorePassword)
                .forCASignedCertificates()
                .build();
        }
    };

    public abstract SSLContext createSSLContext(Path keyStore,
                                                String keyStorePassword,
                                                Path trustStore,
                                                String trustStorePassword,
                                                Path knownHosts) throws GeneralSecurityException, IOException, OperatorCreationException;

    public static Optional<TrustMode> getValueIfPresent(String value) {
        try {
            final TrustMode trustMode = TrustMode.valueOf(value.toUpperCase());
            return Optional.ofNullable(trustMode);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
