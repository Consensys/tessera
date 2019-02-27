package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
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

public class ClientSSLContextFactoryImpl implements ClientSSLContextFactory {

    private static final String DEFAULT_KNOWN_SERVER_FILEPATH = "knownServers";

    private static final EnvironmentVariableProvider envVarProvider = new EnvironmentVariableProvider();

    @Override
    public SSLContext from(String address, SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getClientTrustMode().name())
            .orElse(TrustMode.NONE);

        final Path knownServersFile = Optional.ofNullable(sslConfig.getKnownServersFile())
            .orElse(Paths.get(DEFAULT_KNOWN_SERVER_FILEPATH));

        final SSLContextProperties properties = new SSLContextProperties(
            address,
            sslConfig.getClientKeyStore(),
            getClientKeyStorePassword(sslConfig),
            sslConfig.getClientTlsKeyPath(),
            sslConfig.getClientTlsCertificatePath(),
            sslConfig.getClientTrustStore(),
            getClientTrustStorePassword(sslConfig),
            sslConfig.getClientTrustCertificates(),
            knownServersFile
        );

        try {
            return trustMode.createSSLContext(properties);
        } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
            throw new TesseraSecurityException(ex);
        }
    }

    private String getClientKeyStorePassword(SslConfig sslConfig) {
        String password = envVarProvider.getEnv(EnvironmentVariables.clientKeyStorePwd);

        if(password == null) {
            return sslConfig.getClientKeyStorePassword();
        }

        return password;
    }

    private String getClientTrustStorePassword(SslConfig sslConfig) {
        String password = envVarProvider.getEnv(EnvironmentVariables.clientTrustStorePwd);

        if(password == null) {
            return sslConfig.getClientTrustStorePassword();
        }

        return password;
    }
}
