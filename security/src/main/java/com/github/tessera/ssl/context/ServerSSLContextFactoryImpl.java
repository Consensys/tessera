package com.github.tessera.ssl.context;

import com.github.tessera.config.SslConfig;
import com.github.tessera.ssl.exception.NexusSecurityException;
import com.github.tessera.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;

public class ServerSSLContextFactoryImpl implements ServerSSLContextFactory {

    @Override
    public SSLContext from(SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getServerTrustMode().name())
            .orElse(TrustMode.NONE);

        Path keyStore = sslConfig.getServerKeyStore();
        String keyStorePassword = sslConfig.getServerKeyStorePassword();
        Path trustStore = sslConfig.getServerTrustStore();
        String trustStorePassword = sslConfig.getServerTrustStorePassword();
        Path knownHostsFile = sslConfig.getKnownClientsFile();

        try {
            return trustMode
                    .createSSLContext(keyStore, keyStorePassword, trustStore, trustStorePassword, knownHostsFile);
        } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | CertificateException | KeyStoreException | IOException | OperatorCreationException | NoSuchProviderException | InvalidKeyException | SignatureException ex) {
            throw new NexusSecurityException(ex);
        }
    }
}
