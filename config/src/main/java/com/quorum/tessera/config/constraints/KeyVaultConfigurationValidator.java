package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyData;

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

        boolean hasVaultKeys = false;

        for(KeyData keyData : keyConfiguration.getKeyData()) {
            if(keyData.getKeyVaultId() != null) {
                hasVaultKeys = true;
                break;
            }
        }

        if(hasVaultKeys && keyConfiguration.getKeyVaultConfig() == null) {
            return false;
        }

        return true;
    }
}
