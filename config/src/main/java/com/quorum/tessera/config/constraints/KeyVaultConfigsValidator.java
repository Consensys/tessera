package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.DefaultKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyVaultConfigsValidator implements ConstraintValidator<ValidKeyVaultConfigs, List<DefaultKeyVaultConfig>> {

    private ValidKeyVaultConfigs config;

    @Override
    public void initialize(ValidKeyVaultConfigs config) {
        this.config = config;
    }

    @Override
    public boolean isValid(
            List<DefaultKeyVaultConfig> keyVaultConfigs, ConstraintValidatorContext constraintValidatorContext) {
        if (keyVaultConfigs == null || keyVaultConfigs.size() == 0) {
            return true;
        }

        HashMap<KeyVaultType, Integer> typeCount = new HashMap<>();

        for (KeyVaultConfig c : keyVaultConfigs) {
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
                    .buildConstraintViolationWithTemplate(String.join(" ", entry.getKey().toString(), "{ValidKeyVaultConfigs.message}"))
                    .addConstraintViolation();

                result = false;
            }
        }

        return result;
    }
}
