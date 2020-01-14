package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;

public class NoDuplicateKeyVaultConfigsValidator
        implements ConstraintValidator<NoDuplicateKeyVaultConfigs, KeyConfiguration> {

    private NoDuplicateKeyVaultConfigs config;

    @Override
    public void initialize(NoDuplicateKeyVaultConfigs config) {
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
            if (t == null) {
                continue;
            } else if (typeCount.containsKey(t)) {
                typeCount.put(t, typeCount.get(t) + 1);
            } else {
                typeCount.put(t, 0);
            }
        }

        boolean result = true;

        for (Map.Entry<KeyVaultType, Integer> entry : typeCount.entrySet()) {
            if (entry.getValue() > 0) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(
                                String.join(" ", entry.getKey().toString(), constraintValidatorContext.getDefaultConstraintMessageTemplate()))
                        .addConstraintViolation();

                result = false;
            }
        }

        return result;
    }
}
