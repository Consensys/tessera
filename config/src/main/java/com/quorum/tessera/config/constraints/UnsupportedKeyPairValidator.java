package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.keypairs.UnsupportedKeyPair;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

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
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}")
                .addConstraintViolation();
        }
        else if(isIncompleteInlineKeyPair(keyPair)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothInlineKeysRequired.message}")
                .addConstraintViolation();
        }
        else if(isIncompleteAzureVaultKeyPair(keyPair)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}")
                .addConstraintViolation();
        }
        else if(isIncompleteHashicorpVaultKeyPair(keyPair)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}")
                .addConstraintViolation();
        }
        else if(isIncompleteFilesystemKeyPair(keyPair)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothFilesystemKeysRequired.message}")
                .addConstraintViolation();
        }

        return false;
    }

    private boolean isIncompleteDirectKeyPair(UnsupportedKeyPair keyPair) {
        return isOnlyOneInputNull(keyPair.getPublicKey(), keyPair.getPrivateKey());
    }

    private boolean isIncompleteInlineKeyPair(UnsupportedKeyPair keyPair) {
        return isOnlyOneInputNull(keyPair.getPublicKey(), keyPair.getConfig());
    }

    private boolean isIncompleteAzureVaultKeyPair(UnsupportedKeyPair keyPair) {
        return isOnlyOneInputNull(keyPair.getAzureVaultPublicKeyId(), keyPair.getAzureVaultPrivateKeyId());
    }

    private boolean isIncompleteHashicorpVaultKeyPair(UnsupportedKeyPair keyPair) {
        if(isOnlyOneInputNull(keyPair.getHashicorpVaultPublicKeyId(), keyPair.getHashicorpVaultPrivateKeyId())) {
            return true;
        }

        return isOnlyOneInputNull(keyPair.getHashicorpVaultPublicKeyId(), keyPair.getHashicorpVaultSecretPath());
    }

    private boolean isIncompleteFilesystemKeyPair(UnsupportedKeyPair keyPair) {
        return isOnlyOneInputNull(keyPair.getPublicKeyPath(), keyPair.getPrivateKeyPath());
    }

    private boolean isOnlyOneInputNull(Object obj1, Object obj2) {
        return Objects.isNull(obj1) ^ Objects.isNull(obj2);
    }
}
