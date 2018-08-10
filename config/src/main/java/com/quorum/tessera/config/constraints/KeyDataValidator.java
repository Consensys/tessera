package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.util.FilesDelegate;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class KeyDataValidator implements ConstraintValidator<ValidKeyData, List<KeyData>> {

    private ValidKeyData config;

    private FilesDelegate filesDelegate = FilesDelegate.create();

    @Override
    public void initialize(ValidKeyData config) {
        this.config = config;
    }

    @Override
    public boolean isValid(List<KeyData> keyDataList, ConstraintValidatorContext context) {
        if(keyDataList == null) {
            return true;
        }
        
        for (KeyData keyData : keyDataList) {
            //Assume that test values have been provided. 
            if (keyData.getPublicKeyPath() == null && keyData.getPrivateKeyPath() == null) {
                return true;
            }

            if (keyData.getPublicKeyPath() == null || keyData.getPrivateKeyPath() == null) {

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("When providing key paths, must give both as paths, not just one, and both files must exist")
                        .addConstraintViolation();

                return false;
            }

            if (filesDelegate.notExists(keyData.getPublicKeyPath())) {

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Public key path " + keyData.getPublicKeyPath() + "Does not exist")
                        .addConstraintViolation();

                return false;
            }

            if (filesDelegate.notExists(keyData.getPrivateKeyPath())) {

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Private key path " + keyData.getPrivateKeyPath() + "Does not exist")
                        .addConstraintViolation();

                return false;
            }
        }

        return true;
    }

    public void setFilesDelegate(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }

}
