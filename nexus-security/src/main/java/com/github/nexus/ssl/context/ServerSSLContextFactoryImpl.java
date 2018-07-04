package com.github.nexus.ssl.context;

import com.github.nexus.config.SslConfig;
import com.github.nexus.ssl.exception.NexusSecurityException;
import com.github.nexus.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class ServerSSLContextFactoryImpl implements ServerSSLContextFactory {

    @Override
    public SSLContext from(SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getServerTrustMode().name())
            .orElse(TrustMode.NONE);

        String keyStore = sslConfig.getServerKeyStore().toString();
        String keyStorePassword = sslConfig.getServerKeyStorePassword();
        String trustStore = sslConfig.getServerTrustStore().toString();
        String trustStorePassword = sslConfig.getServerTrustStorePassword();
        String knownHostsFile = sslConfig.getKnownClientsFile().toString();

        try {
            return trustMode
                    .createSSLContext(keyStore, keyStorePassword, trustStore, trustStorePassword, knownHostsFile);
        } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | CertificateException | KeyStoreException | IOException | OperatorCreationException | NoSuchProviderException | InvalidKeyException | SignatureException ex) {
            throw new NexusSecurityException(ex);
        }
    }
}
