
package com.github.tessera.config.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Files;
import java.nio.file.Path;


public class PathValidator implements ConstraintValidator<ValidPath, Path> {

    private ValidPath config;

    @Override
    public void initialize(ValidPath a) {
        this.config = a;
    }

    @Override
    public boolean isValid(Path t, ConstraintValidatorContext cvc) {
        return !config.checkExists() || !Files.notExists(t);
    }

}
