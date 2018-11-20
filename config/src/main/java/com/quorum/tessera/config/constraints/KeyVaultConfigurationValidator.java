package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;

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
            .anyMatch(keyPair -> keyPair instanceof AzureVaultKeyPair);

        if(isUsingVaultKeys && keyConfiguration.getKeyVaultConfig() == null) {
            return false;
        }

        return true;
    }
}
