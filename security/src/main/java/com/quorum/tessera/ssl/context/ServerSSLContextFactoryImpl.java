package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.context.model.SSLContextProperties;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class ServerSSLContextFactoryImpl implements ServerSSLContextFactory {

    @Override
    public SSLContext from(String address, SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getServerTrustMode().name())
            .orElse(TrustMode.NONE);

        final SSLContextProperties properties = new SSLContextProperties(
            address,
            sslConfig.getServerKeyStore(),
            sslConfig.getServerKeyStorePassword(),
            sslConfig.getServerTlsKeyPath(),
            sslConfig.getServerTlsCertificatePath(),
            sslConfig.getServerTrustStore(),
            sslConfig.getServerTrustStorePassword(),
            sslConfig.getServerTrustCertificates(),
            sslConfig.getKnownClientsFile()
        );

        try {
            return trustMode.createSSLContext(properties);
        } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
            throw new TesseraSecurityException(ex);
        }
    }
}
