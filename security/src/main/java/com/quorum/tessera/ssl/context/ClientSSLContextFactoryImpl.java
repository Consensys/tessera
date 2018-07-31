package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.context.model.SSLContextProperties;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class ClientSSLContextFactoryImpl implements ClientSSLContextFactory {

    @Override
    public SSLContext from(String address, SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getClientTrustMode().name())
            .orElse(TrustMode.NONE);

        final SSLContextProperties properties = new SSLContextProperties(
            address,
            sslConfig.getClientKeyStore(),
            sslConfig.getClientKeyStorePassword(),
            sslConfig.getClientTlsKeyPath(),
            sslConfig.getClientTlsCertificatePath(),
            sslConfig.getClientTrustStore(),
            sslConfig.getClientTrustStorePassword(),
            sslConfig.getClientTrustCertificates(),
            sslConfig.getKnownServersFile()
        );

        try {
            return trustMode.createSSLContext(properties);
        } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
            throw new TesseraSecurityException(ex);
        }
    }
}
