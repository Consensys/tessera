package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.io.FilesDelegate;
import java.util.ArrayList;
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
        if (keyDataList == null) {
            return true;
        }

        List<String> errors = new ArrayList<>();

        for (int i = 0; i < keyDataList.size(); i++) {
            final String positionAsString = String.valueOf(i + 1);
            final KeyData keyData = keyDataList.get(i);
            //Assume that test values have been provided. 
            if (keyData.getPublicKeyPath() == null && keyData.getPrivateKeyPath() == null) {
                continue;
            }

            if (keyData.getPublicKeyPath() == null || keyData.getPrivateKeyPath() == null) {
                errors.add(String.format("KeyData %s : When providing key paths, must give both as paths, not just one, and both files must exist",
                        positionAsString));
                continue;
            }

            if (filesDelegate.notExists(keyData.getPublicKeyPath())) {
                errors.add(String.format("KeyData %s : Public key path %s does not exist",
                        positionAsString, keyData.getPublicKeyPath()));

            }

            if (filesDelegate.notExists(keyData.getPrivateKeyPath())) {
                errors.add(String.format("KeyData %s : Private key path %s does not exist",
                        positionAsString, keyData.getPrivateKeyPath()));
            }
            
            
        }

        if (!errors.isEmpty()) {
            context.disableDefaultConstraintViolation();
            errors.forEach(error -> {
                context.buildConstraintViolationWithTemplate(error)
                        .addConstraintViolation();

            });
            return false;

        }

        return true;
    }

    public void setFilesDelegate(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }

}
