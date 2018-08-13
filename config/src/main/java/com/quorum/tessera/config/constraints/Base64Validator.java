package com.quorum.tessera.config.constraints;

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
        //TODO: 
        if("NACL_FAILURE".equals(value)) {
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
