package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.context.model.SSLContextProperties;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class ServerSSLContextFactoryImpl implements ServerSSLContextFactory {

    private static final String DEFAULT_KNOWN_CLIENT_FILEPATH = "knownClients";

    @Override
    public SSLContext from(String address, SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getServerTrustMode().name())
            .orElse(TrustMode.NONE);

        final Path knownClientsFile = Optional.ofNullable(sslConfig.getKnownClientsFile())
            .orElse(Paths.get(DEFAULT_KNOWN_CLIENT_FILEPATH));

        final SSLContextProperties properties = new SSLContextProperties(
            address,
            sslConfig.getServerKeyStore(),
            sslConfig.getServerKeyStorePassword(),
            sslConfig.getServerTlsKeyPath(),
            sslConfig.getServerTlsCertificatePath(),
            sslConfig.getServerTrustStore(),
            sslConfig.getServerTrustStorePassword(),
            sslConfig.getServerTrustCertificates(),
            knownClientsFile
        );

        try {
            return trustMode.createSSLContext(properties);
        } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
            throw new TesseraSecurityException(ex);
        }
    }
}
