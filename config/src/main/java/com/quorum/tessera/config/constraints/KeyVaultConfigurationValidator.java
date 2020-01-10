package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

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

        boolean hasAzureKeyVaultConfig =
                keyConfiguration.getAzureKeyVaultConfig() != null
                        || Optional.ofNullable(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE))
                                .filter((c) -> c.getKeyVaultType().equals(KeyVaultType.AZURE))
                                .isPresent();

        if (isUsingAzureVaultKeys && !hasAzureKeyVaultConfig) {
            cvc.disableDefaultConstraintViolation();
            cvc.buildConstraintViolationWithTemplate("{ValidKeyVaultConfiguration.azure.message}")
                    .addConstraintViolation();

            return false;
        }

        boolean isUsingHashicorpVaultKeys =
                keyConfiguration.getKeyData().stream().anyMatch(keyPair -> keyPair instanceof HashicorpVaultKeyPair);

        boolean hasHashicorpKeyVaultConfig =
                keyConfiguration.getHashicorpKeyVaultConfig() != null
                        || Optional.ofNullable(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP))
                                .filter((c) -> c.getKeyVaultType().equals(KeyVaultType.HASHICORP))
                                .isPresent();

        if (isUsingHashicorpVaultKeys && !hasHashicorpKeyVaultConfig) {
            cvc.disableDefaultConstraintViolation();
            cvc.buildConstraintViolationWithTemplate("{ValidKeyVaultConfiguration.hashicorp.message}")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
