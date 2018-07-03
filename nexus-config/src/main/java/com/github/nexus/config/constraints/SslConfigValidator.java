package com.github.nexus.config.constraints;


import com.github.nexus.config.SslAuthenticationMode;
import com.github.nexus.config.SslConfig;
import com.github.nexus.config.SslTrustMode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Files;
import java.util.Objects;

public class SslConfigValidator implements ConstraintValidator<ValidSsl,SslConfig> {

    private ValidSsl validSsl;

    @Override
    public void initialize(ValidSsl validSsl) {
        this.validSsl = validSsl;
    }

    @Override
    public boolean isValid(SslConfig sslConfig, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        if (validSsl.checkSslValid()){
            if (Objects.isNull(sslConfig)) {
                return true;
            }

            if (sslConfig.getTls() == SslAuthenticationMode.OFF) {
                return true;
            }

            if (sslConfig.getTls() == SslAuthenticationMode.STRICT) {
                if (!sslConfig.isGenerateKeyStoreIfNotExisted()) {
                    if (Objects.isNull(sslConfig.getServerKeyStore()) ||
                        Objects.isNull(sslConfig.getServerKeyStorePassword()) ||
                        Files.notExists(sslConfig.getServerKeyStore())) {
                        setMessage("Server keystore configuration not valid. " +
                            "Please ensure keystore file exists or keystore password not null, " +
                            "otherwise please set keystore generation flag to true to have keystore created", context);
                        return false;
                    }
                    if (Objects.isNull(sslConfig.getClientKeyStore()) ||
                        Objects.isNull(sslConfig.getClientKeyStorePassword()) ||
                        Files.notExists(sslConfig.getClientKeyStore())) {
                        setMessage("Client keystore configuration not valid. " +
                            "Please ensure keystore file exists or keystore password not null, " +
                            "otherwise please set keystore generation flag to true to have keystore created", context);
                        return false;
                    }
                }

                if (Objects.isNull(sslConfig.getServerTrustMode()) || Objects.isNull(sslConfig.getClientTrustMode())) {
                    setMessage("Trust mode does not have valid value. Please check server/client trust mode config", context);
                    return false;
                }

                if (sslConfig.getServerTrustMode() == SslTrustMode.WHITELIST) {
                    if (Objects.isNull(sslConfig.getKnownClientsFile()) ||
                        Files.notExists(sslConfig.getKnownClientsFile())) {
                        setMessage("Known clients file not found. If server trust mode is WHITELIST, known clients file must be provided", context);
                        return false;
                    }
                }

                if (sslConfig.getServerTrustMode() == SslTrustMode.CA) {
                    if (Objects.isNull(sslConfig.getServerTrustStore()) ||
                        Objects.isNull(sslConfig.getServerTrustStorePassword()) ||
                        Files.notExists(sslConfig.getServerTrustStore())) {
                        setMessage("Trust store config not valid. If server trust mode is CA, trust store must be provided", context);
                        return false;
                    }
                }

                if (sslConfig.getClientTrustMode() == SslTrustMode.WHITELIST) {
                    if (Objects.isNull(sslConfig.getKnownServersFile()) ||
                        Files.notExists(sslConfig.getKnownServersFile())) {
                        setMessage("Known servers file not found. If client trust mode is WHITELIST, known servers file must be provided", context);
                        return false;
                    }
                }

                if (sslConfig.getClientTrustMode() == SslTrustMode.CA) {
                    if (Objects.isNull(sslConfig.getClientTrustStore()) ||
                        Objects.isNull(sslConfig.getClientTrustStorePassword()) ||
                        Files.notExists(sslConfig.getClientTrustStore())) {
                        setMessage("Trust store config not valid. If client trust mode is CA, trust store must be provided", context);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void setMessage(final String message, ConstraintValidatorContext context) {
        context
            .buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
