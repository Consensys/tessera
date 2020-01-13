package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.keypairs.AWSKeyPair;
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

        boolean result = true;

        boolean isUsingAzureVaultKeys =
                keyConfiguration.getKeyData().stream().anyMatch(keyPair -> keyPair instanceof AzureVaultKeyPair);

        boolean hasAzureKeyVaultConfig =
                Optional.ofNullable(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).isPresent();

        if (isUsingAzureVaultKeys && !hasAzureKeyVaultConfig) {
            cvc.disableDefaultConstraintViolation();
            cvc.buildConstraintViolationWithTemplate("{ValidKeyVaultConfiguration.azure.message}")
                    .addConstraintViolation();

            result = false;
        }

        boolean isUsingHashicorpVaultKeys =
                keyConfiguration.getKeyData().stream().anyMatch(keyPair -> keyPair instanceof HashicorpVaultKeyPair);

        boolean hasHashicorpKeyVaultConfig =
                Optional.ofNullable(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP)).isPresent();

        if (isUsingHashicorpVaultKeys && !hasHashicorpKeyVaultConfig) {
            cvc.disableDefaultConstraintViolation();
            cvc.buildConstraintViolationWithTemplate("{ValidKeyVaultConfiguration.hashicorp.message}")
                    .addConstraintViolation();

            result = false;
        }

        boolean isUsingAWSVaultKeys =
                keyConfiguration.getKeyData().stream().anyMatch(keyPair -> keyPair instanceof AWSKeyPair);

        boolean hasAWSKeyVaultConfig =
                Optional.ofNullable(keyConfiguration.getKeyVaultConfig(KeyVaultType.AWS)).isPresent();

        if (isUsingAWSVaultKeys && !hasAWSKeyVaultConfig) {
            cvc.disableDefaultConstraintViolation();
            cvc.buildConstraintViolationWithTemplate("{ValidKeyVaultConfiguration.aws.message}")
                    .addConstraintViolation();

            result = false;
        }

        return result;
    }
}
