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
                        return false;
                    }
                    if (Objects.isNull(sslConfig.getClientKeyStore()) ||
                        Objects.isNull(sslConfig.getClientKeyStorePassword()) ||
                        Files.notExists(sslConfig.getClientKeyStore())) {
                        return false;
                    }
                }

                if (Objects.isNull(sslConfig.getServerTrustMode()) || Objects.isNull(sslConfig.getClientTrustMode())) {
                    return false;
                }

                if (sslConfig.getServerTrustMode() == SslTrustMode.WHITELIST) {
                    if (Objects.isNull(sslConfig.getKnownClientsFile()) ||
                        Files.notExists(sslConfig.getKnownClientsFile())) {
                        return false;
                    }
                }

                if (sslConfig.getServerTrustMode() == SslTrustMode.CA) {
                    if (Objects.isNull(sslConfig.getServerTrustStore()) ||
                        Objects.isNull(sslConfig.getServerTrustStorePassword()) ||
                        Files.notExists(sslConfig.getServerTrustStore())) {
                        return false;
                    }
                }

                if (sslConfig.getClientTrustMode() == SslTrustMode.WHITELIST) {
                    if (Objects.isNull(sslConfig.getKnownServersFile()) ||
                        Files.notExists(sslConfig.getKnownServersFile())) {
                        return false;
                    }
                }

                if (sslConfig.getClientTrustMode() == SslTrustMode.CA) {
                    if (Objects.isNull(sslConfig.getClientTrustStore()) ||
                        Objects.isNull(sslConfig.getClientTrustStorePassword()) ||
                        Files.notExists(sslConfig.getClientTrustStore())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
