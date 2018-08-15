package com.quorum.tessera.config.constraints;

import static com.quorum.tessera.config.adapters.KeyDataAdapter.NACL_FAILURE_TOKEN;
import java.util.Base64;
import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class Base64Validator implements ConstraintValidator<ValidBase64, String> {

    @Override
    public void initialize(ValidBase64 a) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cvc) {
        if(Objects.isNull(value)) {
            return true;
        }

        if(value.startsWith(NACL_FAILURE_TOKEN)) {
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
