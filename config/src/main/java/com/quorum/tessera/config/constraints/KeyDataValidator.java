package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.io.FilesDelegate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class KeyDataValidator implements ConstraintValidator<ValidKeyData, KeyData> {

    private ValidKeyData config;

    private FilesDelegate filesDelegate = FilesDelegate.create();

    @Override
    public void initialize(ValidKeyData config) {
        this.config = config;
    }

    @Override
    public boolean isValid(KeyData keyData, ConstraintValidatorContext context) {
        if (keyData == null) {
            return true;
        }

        //TODO This class is not used any more, need to incorporate these validation messages into the default key pair type to be used in the KeyDataAdapter

        if(keyData.getPublicKey() != null && keyData.getPrivateKey() == null && keyData.getAzureVaultPrivateKeyId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{ValidKeyData.bothPrivateAndPublicRequired.message}")
                .addConstraintViolation();
            return false;
        }

        //Assume that test values have been provided.
        if (keyData.getPublicKeyPath() == null && keyData.getPrivateKeyPath() == null) {
            return true;
        }

        if (keyData.getPublicKeyPath() == null || keyData.getPrivateKeyPath() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{ValidKeyData.bothOrNoPathsRequired}")
                .addConstraintViolation();
            return false;
        }

        if (filesDelegate.notExists(keyData.getPublicKeyPath())) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("{ValidKeyData.publicKeyPath.notExists}")
                    .addNode("publicKeyPath")
                    .addConstraintViolation();
            return false;
        }

        if (filesDelegate.notExists(keyData.getPrivateKeyPath())) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("{ValidKeyData.privateKeyPath.notExists}")
                    .addNode("privateKeyPath")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    public void setFilesDelegate(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }

}
