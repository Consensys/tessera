package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class KeyVaultConfigurationValidator
        implements ConstraintValidator<ValidKeyVaultConfiguration, KeyConfiguration> {

    @Override
    public boolean isValid(KeyConfiguration keyConfiguration, ConstraintValidatorContext cvc) {

        // return true so that only NotNull annotation creates violation
        if (keyConfiguration == null) {
            return true;
        }

        boolean isUsingAzureVaultKeys =
                keyConfiguration.getKeyData().stream().anyMatch(keyPair -> keyPair instanceof AzureVaultKeyPair);

        if (isUsingAzureVaultKeys && keyConfiguration.getAzureKeyVaultConfig() == null) {
            cvc.disableDefaultConstraintViolation();
            cvc.buildConstraintViolationWithTemplate("{ValidKeyVaultConfiguration.azure.message}")
                    .addConstraintViolation();

            return false;
        }

        boolean isUsingHashicorpVaultKeys =
                keyConfiguration.getKeyData().stream().anyMatch(keyPair -> keyPair instanceof HashicorpVaultKeyPair);

        if (isUsingHashicorpVaultKeys && keyConfiguration.getHashicorpKeyVaultConfig() == null) {
            cvc.disableDefaultConstraintViolation();
            cvc.buildConstraintViolationWithTemplate("{ValidKeyVaultConfiguration.hashicorp.message}")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
