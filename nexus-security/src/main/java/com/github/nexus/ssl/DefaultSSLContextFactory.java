package com.github.nexus.ssl;

import com.github.nexus.config.SslConfig;
import com.github.nexus.config.SslTrustMode;
import com.github.nexus.ssl.strategy.TrustMode;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import org.bouncycastle.operator.OperatorCreationException;

public class DefaultSSLContextFactory implements SSLContextFactory {

    @Override
    public SSLContext from(SslConfig sslConfig) {

        SslTrustMode sslTrustMode = Optional.ofNullable(sslConfig.getServerTrustMode())
                .orElse(SslTrustMode.NONE);

        TrustMode trustMode = Stream.of(TrustMode.values())
                .filter(tm -> Objects.equals(tm.name(), sslTrustMode.name()))
                .findAny().get();

        String keyStore = sslConfig.getServerKeyStore().toString();
        String keyStorePassword = sslConfig.getServerKeyStorePassword();
        String trustStore = sslConfig.getServerTrustStore().toString();
        String trustStorePassword = sslConfig.getServerTrustStorePassword();
        String knownHostsFile = sslConfig.getKnownServersFile().toString();

        try {
            return trustMode
                    .createSSLContext(keyStore, keyStorePassword, trustStore, trustStorePassword, knownHostsFile);
        } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | CertificateException | KeyStoreException | IOException | OperatorCreationException | NoSuchProviderException | InvalidKeyException | SignatureException ex) {
            throw new NexusSecurityException(ex);
        }
    }
}
