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

public class ServerSSLContextFactoryImpl implements ServerSSLContextFactory {

  private static final String DEFAULT_KNOWN_CLIENT_FILEPATH = "knownClients";

  private final EnvironmentVariableProvider environmentVariableProvider;

  protected ServerSSLContextFactoryImpl(EnvironmentVariableProvider environmentVariableProvider) {
    this.environmentVariableProvider = Objects.requireNonNull(environmentVariableProvider);
  }

  public ServerSSLContextFactoryImpl() {
    this(EnvironmentVariableProviderFactory.load().create());
  }

  @Override
  public SSLContext from(String address, SslConfig sslConfig) {

    TrustMode trustMode =
        TrustMode.getValueIfPresent(sslConfig.getServerTrustMode().name()).orElse(TrustMode.NONE);

    final Path knownClientsFile =
        Optional.ofNullable(sslConfig.getKnownClientsFile())
            .orElse(Paths.get(DEFAULT_KNOWN_CLIENT_FILEPATH));

    final SSLContextProperties properties =
        new SSLContextProperties(
            address,
            sslConfig.getServerKeyStore(),
            getServerKeyStorePassword(sslConfig),
            sslConfig.getServerTlsKeyPath(),
            sslConfig.getServerTlsCertificatePath(),
            sslConfig.getServerTrustStore(),
            getServerTrustStorePassword(sslConfig),
            sslConfig.getServerTrustCertificates(),
            knownClientsFile);

    try {
      return trustMode.createSSLContext(properties);
    } catch (IOException | OperatorCreationException | GeneralSecurityException ex) {
      throw new TesseraSecurityException(ex);
    }
  }

  // TODO - Package private for testing, refactor so this can be made private
  char[] getServerKeyStorePassword(SslConfig sslConfig) {
    return getPreferredPassword(
        sslConfig.getServerKeyStorePassword(),
        sslConfig.getEnvironmentVariablePrefix(),
        EnvironmentVariables.SERVER_KEYSTORE_PWD);
  }

  // TODO - Package private for testing, refactor so this can be made private
  char[] getServerTrustStorePassword(SslConfig sslConfig) {
    return getPreferredPassword(
        sslConfig.getServerTrustStorePassword(),
        sslConfig.getEnvironmentVariablePrefix(),
        EnvironmentVariables.SERVER_TRUSTSTORE_PWD);
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
