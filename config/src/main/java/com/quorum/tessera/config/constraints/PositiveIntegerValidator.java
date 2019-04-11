package com.quorum.tessera.config.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PositiveIntegerValidator implements ConstraintValidator<ValidPositiveInteger, String> {

    @Override
    public boolean isValid(String secretVersion, ConstraintValidatorContext constraintValidatorContext) {
        if (secretVersion == null) {
            return true;
        }

        Integer i;

        try {
            i = Integer.valueOf(secretVersion);
        } catch (NumberFormatException e) {
            return false;
        }

        return i >= 0;
    }

}
