package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class AzureVaultKeyPairValidator implements ConstraintValidator<ValidAzureVaultKeyPair, AzureVaultKeyPair> {

    private ValidAzureVaultKeyPair annotation;

    @Override
    public void initialize(ValidAzureVaultKeyPair annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean isValid(AzureVaultKeyPair azureVaultKeyPair, ConstraintValidatorContext cvc) {

        if (azureVaultKeyPair == null) {
            return true;
        }

        return Objects.isNull(azureVaultKeyPair.getPublicKeyVersion()) == Objects.isNull(azureVaultKeyPair.getPrivateKeyVersion());

    }

}
