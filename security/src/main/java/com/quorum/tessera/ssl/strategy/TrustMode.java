package com.quorum.tessera.ssl.strategy;

import com.quorum.tessera.ssl.context.SSLContextBuilder;
import com.quorum.tessera.ssl.context.model.SSLContextProperties;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.bouncycastle.operator.OperatorCreationException;

public enum TrustMode {
  NONE {
    @Override
    public SSLContext createSSLContext(SSLContextProperties properties)
        throws GeneralSecurityException, OperatorCreationException, IOException {
      return SSLContextBuilder.createBuilder(
              properties.getAddress(),
              properties.getKeyStore(),
              properties.getKeyStorePassword(),
              properties.getTrustStore(),
              properties.getTrustStorePassword())
          .fromPemFiles(
              properties.getKey(), properties.getCertificate(), properties.getTrustedCertificates())
          .forAllCertificates()
          .build();
    }
  },

  WHITELIST {
    @Override
    public SSLContext createSSLContext(SSLContextProperties properties)
        throws GeneralSecurityException, IOException, OperatorCreationException {
      return SSLContextBuilder.createBuilder(
              properties.getAddress(),
              properties.getKeyStore(),
              properties.getKeyStorePassword(),
              properties.getTrustStore(),
              properties.getTrustStorePassword())
          .fromPemFiles(
              properties.getKey(), properties.getCertificate(), properties.getTrustedCertificates())
          .forWhiteList(properties.getKnownHosts())
          .build();
    }
  },

  TOFU {
    @Override
    public SSLContext createSSLContext(SSLContextProperties properties)
        throws GeneralSecurityException, IOException, OperatorCreationException {
      return SSLContextBuilder.createBuilder(
              properties.getAddress(),
              properties.getKeyStore(),
              properties.getKeyStorePassword(),
              properties.getTrustStore(),
              properties.getTrustStorePassword())
          .fromPemFiles(
              properties.getKey(), properties.getCertificate(), properties.getTrustedCertificates())
          .forTrustOnFirstUse(properties.getKnownHosts())
          .build();
    }
  },

  CA {
    @Override
    public SSLContext createSSLContext(SSLContextProperties properties)
        throws GeneralSecurityException, IOException, OperatorCreationException {
      return SSLContextBuilder.createBuilder(
              properties.getAddress(),
              properties.getKeyStore(),
              properties.getKeyStorePassword(),
              properties.getTrustStore(),
              properties.getTrustStorePassword())
          .fromPemFiles(
              properties.getKey(), properties.getCertificate(), properties.getTrustedCertificates())
          .forCASignedCertificates()
          .build();
    }
  },

  CA_OR_TOFU {
    @Override
    public SSLContext createSSLContext(SSLContextProperties properties)
        throws GeneralSecurityException, IOException, OperatorCreationException {
      return SSLContextBuilder.createBuilder(
              properties.getAddress(),
              properties.getKeyStore(),
              properties.getKeyStorePassword(),
              properties.getTrustStore(),
              properties.getTrustStorePassword())
          .fromPemFiles(
              properties.getKey(), properties.getCertificate(), properties.getTrustedCertificates())
          .forCAOrTOFU(properties.getKnownHosts())
          .build();
    }
  };

  public abstract SSLContext createSSLContext(SSLContextProperties properties)
      throws GeneralSecurityException, IOException, OperatorCreationException;

  public static Optional<TrustMode> getValueIfPresent(String value) {
    try {
      final TrustMode trustMode = TrustMode.valueOf(value.toUpperCase());
      return Optional.ofNullable(trustMode);
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
