package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyType;
import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class KeyDataConfigValidator implements ConstraintValidator<ValidKeyDataConfig, KeyDataConfig> {

    private ValidKeyDataConfig annotation;

    @Override
    public void initialize(ValidKeyDataConfig annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean isValid(KeyDataConfig keyDataConfig, ConstraintValidatorContext cvc) {

        if (keyDataConfig == null) {
            return true;
        }

        if (keyDataConfig.getType() == null) {
            return true;
        }

        if (keyDataConfig.getType() == PrivateKeyType.UNLOCKED) {
            return true;
        }

        return Objects.nonNull(keyDataConfig.getPassword());

    }

}
