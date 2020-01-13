package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;

public class KeyVaultConfigsValidator implements ConstraintValidator<ValidKeyVaultConfigs, KeyConfiguration> {

    private ValidKeyVaultConfigs config;

    @Override
    public void initialize(ValidKeyVaultConfigs config) {
        this.config = config;
    }

    @Override
    public boolean isValid(KeyConfiguration keyConfiguration, ConstraintValidatorContext constraintValidatorContext) {
        // cannot have duplicates if the KeyVaultConfigs list is empty
        if (keyConfiguration == null
                || keyConfiguration.getKeyVaultConfigs() == null
                || keyConfiguration.getKeyVaultConfigs().isEmpty()) {
            return true;
        }

        HashMap<KeyVaultType, Integer> typeCount = new HashMap<>();

        if (keyConfiguration.getAzureKeyVaultConfig() != null) {
            typeCount.put(KeyVaultType.AZURE, 0);
        }

        if (keyConfiguration.getHashicorpKeyVaultConfig() != null) {
            typeCount.put(KeyVaultType.HASHICORP, 0);
        }

        for (KeyVaultConfig c : keyConfiguration.getKeyVaultConfigs()) {
            final KeyVaultType t = c.getKeyVaultType();
            if (typeCount.containsKey(t)) {
                typeCount.put(t, typeCount.get(t) + 1);
            } else {
                typeCount.put(t, 0);
            }
        }

        boolean result = true;

        for (Map.Entry<KeyVaultType, Integer> entry : typeCount.entrySet()) {
            if (entry.getValue() > 0) {
                String message = "More than one KeyVaultConfig with type " + entry.getKey().toString();

                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(
                                String.join(" ", entry.getKey().toString(), "{ValidKeyVaultConfigs.message}"))
                        .addConstraintViolation();

                result = false;
            }
        }

        return result;
    }
}
