package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslConfigType;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;
import com.quorum.tessera.config.util.EnvironmentVariables;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.Files;
import java.util.Objects;

public class SslConfigValidator implements ConstraintValidator<ValidSsl, SslConfig> {

  private final EnvironmentVariableProvider envVarProvider;

  public SslConfigValidator() {
    this(EnvironmentVariableProviderFactory.load().create());
  }

  public SslConfigValidator(EnvironmentVariableProvider envVarProvider) {
    this.envVarProvider = envVarProvider;
  }

  @Override
  public boolean isValid(SslConfig sslConfig, ConstraintValidatorContext context) {

    context.disableDefaultConstraintViolation();

    if (Objects.isNull(sslConfig)) {
      return true;
    }

    if (sslConfig.getTls() == SslAuthenticationMode.STRICT) {

      if (!sslConfig.isGenerateKeyStoreIfNotExisted()) {

        if (!isServerKeyStoreConfigValid(sslConfig, context)
            || !isClientKeyStoreConfigValid(sslConfig, context)) {
          return false;
        }
      }

      if (!isTrustModeConfigValid(sslConfig, context)) {
        return false;
      }

      if (!isServerConfigValidForWhiteListMode(sslConfig, context)) {
        return false;
      }

      if (!isServerConfigValidForCAMode(sslConfig, context)) {
        return false;
      }

      if (!isClientConfigValidForWhiteListMode(sslConfig, context)) {
        return false;
      }

      if (!isClientConfigValidForCAMode(sslConfig, context)) {
        return false;
      }
    }

    return true;
  }

  private boolean isServerKeyStoreConfigValid(
      SslConfig sslConfig, ConstraintValidatorContext context) {
    if (SslConfigType.CLIENT_ONLY == sslConfig.getSslConfigType()) {
      return true;
    }
    if (Objects.isNull(sslConfig.getServerKeyStore())
        || !isPasswordProvided(
            sslConfig.getServerKeyStorePassword(),
            sslConfig.getEnvironmentVariablePrefix(),
            EnvironmentVariables.SERVER_KEYSTORE_PWD)
        || Files.notExists(sslConfig.getServerKeyStore())) {
      if (Objects.isNull(sslConfig.getServerTlsKeyPath())
          || Objects.isNull(sslConfig.getServerTlsCertificatePath())
          || Files.notExists(sslConfig.getServerTlsKeyPath())
          || Files.notExists(sslConfig.getServerTlsCertificatePath())) {
        setMessage(
            "Server keystore configuration not valid. "
                + "Please ensure keystore file exists or keystore password not null, "
                + "otherwise please set keystore generation flag to true to have keystore created",
            context);
        return false;
      }
    }
    return true;
  }

  private boolean isClientKeyStoreConfigValid(
      SslConfig sslConfig, ConstraintValidatorContext context) {
    if (SslConfigType.SERVER_ONLY == sslConfig.getSslConfigType()) {
      return true;
    }
    if (Objects.isNull(sslConfig.getClientKeyStore())
        || !isPasswordProvided(
            sslConfig.getClientKeyStorePassword(),
            sslConfig.getEnvironmentVariablePrefix(),
            EnvironmentVariables.CLIENT_KEYSTORE_PWD)
        || Files.notExists(sslConfig.getClientKeyStore())) {
      if (Objects.isNull(sslConfig.getClientTlsKeyPath())
          || Objects.isNull(sslConfig.getClientTlsCertificatePath())
          || Files.notExists(sslConfig.getClientTlsKeyPath())
          || Files.notExists(sslConfig.getClientTlsCertificatePath())) {
        setMessage(
            "Client keystore configuration not valid. "
                + "Please ensure keystore file exists or keystore password not null, "
                + "otherwise please set keystore generation flag to true to have keystore created",
            context);
        return false;
      }
    }
    return true;
  }

  private boolean isTrustModeConfigValid(SslConfig sslConfig, ConstraintValidatorContext context) {
    if ((Objects.isNull(sslConfig.getServerTrustMode())
            && sslConfig.getSslConfigType() != SslConfigType.CLIENT_ONLY)
        || (Objects.isNull(sslConfig.getClientTrustMode())
            && sslConfig.getSslConfigType() != SslConfigType.SERVER_ONLY)) {
      setMessage(
          "Trust mode does not have valid value. Please check server/client trust mode config",
          context);
      return false;
    }
    return true;
  }

  private boolean isServerConfigValidForWhiteListMode(
      SslConfig sslConfig, ConstraintValidatorContext context) {
    if (sslConfig.getServerTrustMode() == SslTrustMode.WHITELIST) {
      if (Objects.isNull(sslConfig.getKnownClientsFile())
          || Files.notExists(sslConfig.getKnownClientsFile())) {
        setMessage(
            "Known clients file not found. If server trust mode is WHITELIST, known clients file must be provided",
            context);
        return false;
      }
    }
    return true;
  }

  private boolean isServerConfigValidForCAMode(
      SslConfig sslConfig, ConstraintValidatorContext context) {
    if (sslConfig.getServerTrustMode() == SslTrustMode.CA) {
      if (Objects.isNull(sslConfig.getServerTrustStore())
          || !isPasswordProvided(
              sslConfig.getServerTrustStorePassword(),
              sslConfig.getEnvironmentVariablePrefix(),
              EnvironmentVariables.SERVER_TRUSTSTORE_PWD)
          || Files.notExists(sslConfig.getServerTrustStore())) {
        if (Objects.isNull(sslConfig.getServerTrustCertificates())) {
          setMessage(
              "Trust store config not valid. If server trust mode is CA, trust store must exist and not be null",
              context);
          return false;
        }
      }
    }
    return true;
  }

  private boolean isClientConfigValidForWhiteListMode(
      SslConfig sslConfig, ConstraintValidatorContext context) {
    if (sslConfig.getClientTrustMode() == SslTrustMode.WHITELIST) {
      if (Objects.isNull(sslConfig.getKnownServersFile())
          || Files.notExists(sslConfig.getKnownServersFile())) {
        setMessage(
            "Known servers file not found. If client trust mode is WHITELIST, known servers file must be provided",
            context);
        return false;
      }
    }
    return true;
  }

  private boolean isClientConfigValidForCAMode(
      SslConfig sslConfig, ConstraintValidatorContext context) {
    if (sslConfig.getClientTrustMode() == SslTrustMode.CA) {
      if (Objects.isNull(sslConfig.getClientTrustStore())
          || !isPasswordProvided(
              sslConfig.getClientTrustStorePassword(),
              sslConfig.getEnvironmentVariablePrefix(),
              EnvironmentVariables.CLIENT_TRUSTSTORE_PWD)
          || Files.notExists(sslConfig.getClientTrustStore())) {
        if (Objects.isNull(sslConfig.getClientTrustCertificates())) {
          setMessage(
              "Trust store config not valid. If client trust mode is CA, trust store must exist and not be null",
              context);
          return false;
        }
      }
    }
    return true;
  }

  private boolean isPasswordProvided(char[] configPassword, String envVarPrefix, String envVar) {
    return configPassword != null
        || envVarProvider.hasEnv(envVar)
        || envVarProvider.hasEnv(envVarPrefix + "_" + envVar);
  }

  private void setMessage(final String message, ConstraintValidatorContext context) {
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
