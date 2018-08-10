
package com.quorum.tessera.config.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


public class PathValidator implements ConstraintValidator<ValidPath, Path> {

    private ValidPath config;

    @Override
    public void initialize(ValidPath a) {
        this.config = a;
    }

    @Override
    public boolean isValid(Path t, ConstraintValidatorContext cvc) {
        //Not null deals with this
        if(Objects.isNull(t)) {
            return true;
        }
        
        return !config.checkExists() || !Files.notExists(t);
    }

}
