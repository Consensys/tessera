package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

public class ClientSSLContextFactoryImpl implements ClientSSLContextFactory {

    @Override
    public SSLContext from(SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getClientTrustMode().name())
            .orElse(TrustMode.NONE);

        final Path keyStore = sslConfig.getClientKeyStore();
        final String keyStorePassword = sslConfig.getClientKeyStorePassword();
        final Path trustStore = sslConfig.getClientTrustStore();
        final String trustStorePassword = sslConfig.getClientTrustStorePassword();
        final Path knownHostsFile = sslConfig.getKnownServersFile();

        try {
            return trustMode
                .createSSLContext(keyStore, keyStorePassword, trustStore, trustStorePassword, knownHostsFile);
        } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
            throw new TesseraSecurityException(ex);
        }
    }
}
