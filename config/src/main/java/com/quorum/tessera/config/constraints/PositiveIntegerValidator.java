package com.quorum.tessera.config.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PositiveIntegerValidator implements ConstraintValidator<ValidPositiveInteger, String> {


    private ValidPositiveInteger validPositiveInteger;

    @Override
    public void initialize(ValidPositiveInteger constraintAnnotation) {
        this.validPositiveInteger = constraintAnnotation;
    }

    @Override
    public boolean isValid(String secretVersion, ConstraintValidatorContext constraintValidatorContext) {
        if(secretVersion == null) {
            return true;
        }

        Integer i;

        try {
            i = Integer.valueOf(secretVersion);
        } catch(NumberFormatException e) {
            return false;
        }

        if(i < 0) {
            return false;
        }

        return true;
    }
}
