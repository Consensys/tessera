package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keypairs.InlineKeypair;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class InlineKeypairValidator implements ConstraintValidator<ValidInlineKeypair, InlineKeypair> {

    private ValidInlineKeypair annotation;

    @Override
    public void initialize(ValidInlineKeypair annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean isValid(InlineKeypair inlineKeypair, ConstraintValidatorContext cvc) {

        if (inlineKeypair == null) {
            return true;
        }

        if (inlineKeypair.getPrivateKeyConfig().getType() == null) {
            return true;
        }

        if (inlineKeypair.getPrivateKeyConfig().getType() == PrivateKeyType.UNLOCKED) {
            return true;
        }

        return !inlineKeypair.getPassword().isEmpty();

    }

}
