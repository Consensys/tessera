package com.quorum.tessera.context;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.keypairs.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.enclave.KeyPairConverter;
import com.quorum.tessera.encryption.KeyPair;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

public interface RuntimeContextFactory {

    default RuntimeContext create(Config config) {

        EncryptorConfig encryptorConfig = config.getEncryptor();
        KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(encryptorConfig);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        if(config.getKeys().getKeyVaultConfigs() != null
            && !config.getKeys().getKeyVaultConfigs().isEmpty()) {

            List<KeyVaultConfig> keyVaultConfigs = config.getKeys().getKeyVaultConfigs()
                .stream()
                .map(KeyVaultConfig.class::cast)
                .collect(Collectors.toList());

            Set<ConstraintViolation<?>> violations = keyVaultConfigs.stream()
                .map(validator::validate)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

            if(!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }


        List<ConfigKeyPair> configKeyPairs = config.getKeys().getKeyData().stream()
            .map(o -> KeyDataUtil.unmarshal(o,keyEncryptor))
            .collect(Collectors.toList());

        Set<ConstraintViolation<?>> violations = configKeyPairs.stream()
            .map(validator::validate)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        EnvironmentVariableProvider environmentVariableProvider = new EnvironmentVariableProvider();

        KeyPairConverter keyPairConverter = new KeyPairConverter(config,environmentVariableProvider);

        Collection<KeyPair> pairs = keyPairConverter.convert(configKeyPairs);



        return null;
    }





}
