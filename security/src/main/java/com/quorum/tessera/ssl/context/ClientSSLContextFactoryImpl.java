package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;
import com.quorum.tessera.config.util.EnvironmentVariables;
import com.quorum.tessera.ssl.context.model.SSLContextProperties;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.strategy.TrustMode;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.bouncycastle.operator.OperatorCreationException;

public class ClientSSLContextFactoryImpl implements ClientSSLContextFactory {

  private static final String DEFAULT_KNOWN_SERVER_FILEPATH = "knownServers";

  private final EnvironmentVariableProvider environmentVariableProvider;

  public ClientSSLContextFactoryImpl() {
    this(EnvironmentVariableProviderFactory.load().create());
  }

  protected ClientSSLContextFactoryImpl(EnvironmentVariableProvider environmentVariableProvider) {
    this.environmentVariableProvider = Objects.requireNonNull(environmentVariableProvider);
  }

  @Override
  public SSLContext from(String address, SslConfig sslConfig) {

    TrustMode trustMode =
        TrustMode.getValueIfPresent(sslConfig.getClientTrustMode().name()).orElse(TrustMode.NONE);

    final Path knownServersFile =
        Optional.ofNullable(sslConfig.getKnownServersFile())
            .orElse(Paths.get(DEFAULT_KNOWN_SERVER_FILEPATH));

    final SSLContextProperties properties =
        new SSLContextProperties(
            address,
            sslConfig.getClientKeyStore(),
            getClientKeyStorePassword(sslConfig),
            sslConfig.getClientTlsKeyPath(),
            sslConfig.getClientTlsCertificatePath(),
            sslConfig.getClientTrustStore(),
            getClientTrustStorePassword(sslConfig),
            sslConfig.getClientTrustCertificates(),
            knownServersFile);

    try {
      return trustMode.createSSLContext(properties);
    } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
      throw new TesseraSecurityException(ex);
    }
  }

  // TODO - Package private for testing, refactor so this can be made private
  char[] getClientKeyStorePassword(SslConfig sslConfig) {
    return getPreferredPassword(
        sslConfig.getClientKeyStorePassword(),
        sslConfig.getEnvironmentVariablePrefix(),
        EnvironmentVariables.CLIENT_KEYSTORE_PWD);
  }

  // TODO - Package private for testing, refactor so this can be made private
  char[] getClientTrustStorePassword(SslConfig sslConfig) {
    return getPreferredPassword(
        sslConfig.getClientTrustStorePassword(),
        sslConfig.getEnvironmentVariablePrefix(),
        EnvironmentVariables.CLIENT_TRUSTSTORE_PWD);
  }

  // Return the prefixed env var value if set, else return the config value, else return the global
  // env var value
  private char[] getPreferredPassword(char[] configPassword, String envVarPrefix, String envVar) {

    if (Objects.nonNull(envVarPrefix) && Objects.nonNull(envVar)) {
      char[] password =
          environmentVariableProvider.getEnvAsCharArray(envVarPrefix.concat("_").concat(envVar));
      if (password != null) {
        return password;
      }
    }

    if (Objects.nonNull(configPassword)) {
      return configPassword;
    }

    return environmentVariableProvider.getEnvAsCharArray(envVar);
  }
}
