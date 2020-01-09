package com.quorum.tessera.config.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;
import java.util.Objects;

import static com.quorum.tessera.config.adapters.KeyDataAdapter.NACL_FAILURE_TOKEN;

public class Base64Validator implements ConstraintValidator<ValidBase64, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cvc) {
        if (Objects.isNull(value)) {
            return true;
        }

        if (value.startsWith(NACL_FAILURE_TOKEN)) {
            return true;
        }

        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
