package com.github.nexus.ssl.context;

import com.github.nexus.config.SslConfig;
import com.github.nexus.ssl.exception.NexusSecurityException;
import com.github.nexus.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;

public class ClientSSLContextFactoryImpl implements ClientSSLContextFactory {

    @Override
    public SSLContext from(SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getClientTrustMode().name())
            .orElse(TrustMode.NONE);

        Path keyStore = sslConfig.getClientKeyStore();
        String keyStorePassword = sslConfig.getClientKeyStorePassword();
        Path trustStore = sslConfig.getClientTrustStore();
        String trustStorePassword = sslConfig.getClientTrustStorePassword();
        Path knownHostsFile = sslConfig.getKnownServersFile();

        try {
            return trustMode
                .createSSLContext(keyStore, keyStorePassword, trustStore, trustStorePassword, knownHostsFile);
        } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | CertificateException | KeyStoreException | IOException | OperatorCreationException | NoSuchProviderException | InvalidKeyException | SignatureException ex) {
            throw new NexusSecurityException(ex);
        }
    }
}
