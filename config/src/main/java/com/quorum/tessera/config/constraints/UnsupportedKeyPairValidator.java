package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.keypairs.UnsupportedKeyPair;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UnsupportedKeyPairValidator implements ConstraintValidator<ValidUnsupportedKeyPair, UnsupportedKeyPair> {

    private ValidUnsupportedKeyPair annotation;

    @Override
    public void initialize(ValidUnsupportedKeyPair annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean isValid(UnsupportedKeyPair keyPair, ConstraintValidatorContext context) {
        if(isIncompleteDirectKeyPair(keyPair)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired}")
                .addConstraintViolation();
        }
        else if(isIncompleteInlineKeyPair(keyPair)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothInlineKeysRequired}")
                .addConstraintViolation();
        }
        else if(isIncompleteAzureVaultKeyPair(keyPair)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired}")
                .addConstraintViolation();
        }
        else if(isIncompleteFilesystemKeyPair(keyPair)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothFilesystemKeysRequired}")
                .addConstraintViolation();
        }

        return false;
    }

    private boolean isIncompleteDirectKeyPair(UnsupportedKeyPair keyPair) {
        if(keyPair.getPublicKey() != null && keyPair.getPrivateKey() == null) {
            return true;
        }

        if(keyPair.getPublicKey() == null && keyPair.getPrivateKey() != null) {
            return true;
        }

        return false;
    }

    private boolean isIncompleteInlineKeyPair(UnsupportedKeyPair keyPair) {
        if(keyPair.getPublicKey() == null && keyPair.getConfig() != null) {
            return true;
        }

        return false;
    }

    private boolean isIncompleteAzureVaultKeyPair(UnsupportedKeyPair keyPair) {
        if(keyPair.getAzureVaultPublicKeyId() != null && keyPair.getAzureVaultPrivateKeyId() == null) {
            return true;
        }

        if(keyPair.getAzureVaultPublicKeyId() == null && keyPair.getAzureVaultPrivateKeyId() != null) {
            return true;
        }

        return false;
    }

    private boolean isIncompleteFilesystemKeyPair(UnsupportedKeyPair keyPair) {
        if(keyPair.getPublicKeyPath() != null && keyPair.getPrivateKeyPath() == null) {
            return true;
        }

        if(keyPair.getPublicKeyPath() == null && keyPair.getPrivateKeyPath() != null) {
            return true;
        }

        return false;
    }
}
