package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.ConfigKeyPairType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class KeyVaultConfigurationValidator implements ConstraintValidator<ValidKeyVaultConfiguration, KeyConfiguration> {

    private ValidKeyVaultConfiguration annotation;

    @Override
    public void initialize(ValidKeyVaultConfiguration annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean isValid(KeyConfiguration keyConfiguration, ConstraintValidatorContext cvc) {

        //return true so that only NotNull annotation creates violation
        if(keyConfiguration == null) {
            return true;
        }

        boolean isUsingVaultKeys = keyConfiguration.getKeyData()
            .stream()
            .map(ConfigKeyPair::getType)
            .anyMatch(ConfigKeyPairType.AZURE::equals);

        if(isUsingVaultKeys && keyConfiguration.getAzureKeyVaultConfig() == null) {
            return false;
        }

        return true;
    }
}
