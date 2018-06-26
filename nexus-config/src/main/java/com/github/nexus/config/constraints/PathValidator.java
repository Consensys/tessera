
package com.github.nexus.config.constraints;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class PathValidator implements ConstraintValidator<ValidPath,Path> {
    
    private ValidPath config;
    
    @Override
    public void initialize(ValidPath a) {
        this.config = a;
    }

    @Override
    public boolean isValid(Path t, ConstraintValidatorContext cvc) {
        if(config.checkExists() && Files.notExists(t)) {
            return false;
        }
        
        return true;
    }
    
}
