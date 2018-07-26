package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

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
        } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
            throw new TesseraSecurityException(ex);
        }
    }
}
