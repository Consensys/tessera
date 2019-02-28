package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;
import com.quorum.tessera.config.util.EnvironmentVariables;
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

    private static final EnvironmentVariableProvider envVarProvider = EnvironmentVariableProviderFactory.load().create();

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
            getServerKeyStorePassword(sslConfig),
            sslConfig.getServerTlsKeyPath(),
            sslConfig.getServerTlsCertificatePath(),
            sslConfig.getServerTrustStore(),
            getServerTrustStorePassword(sslConfig),
            sslConfig.getServerTrustCertificates(),
            knownClientsFile
        );

        try {
            return trustMode.createSSLContext(properties);
        } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
            throw new TesseraSecurityException(ex);
        }
    }

    // TODO - Package private for testing, refactor so this can be made private
    String getServerKeyStorePassword(SslConfig sslConfig) {
        String password = envVarProvider.getEnv(EnvironmentVariables.serverKeyStorePwd);

        if(password == null) {
            return sslConfig.getServerKeyStorePassword();
        }

        return password;
    }

    // TODO - Package private for testing, refactor so this can be made private
    String getServerTrustStorePassword(SslConfig sslConfig) {
        String password = envVarProvider.getEnv(EnvironmentVariables.serverTrustStorePwd);

        if(password == null) {
            return sslConfig.getServerTrustStorePassword();
        }

        return password;
    }
}
