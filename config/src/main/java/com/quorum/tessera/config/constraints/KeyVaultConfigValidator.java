package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.DefaultKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Define here to be used during path validation
@ValidPath(checkExists = true, message = "File does not exist")
public class KeyVaultConfigValidator implements ConstraintValidator<ValidKeyVaultConfig, DefaultKeyVaultConfig> {

    private ValidKeyVaultConfig config;

    @Override
    public void initialize(ValidKeyVaultConfig config) {
        this.config = config;
    }

    @Override
    public boolean isValid(
            DefaultKeyVaultConfig keyVaultConfig, ConstraintValidatorContext constraintValidatorContext) {
        if (keyVaultConfig == null || keyVaultConfig.getKeyVaultType() == null) {
            return true;
        }

        KeyVaultType keyVaultType = keyVaultConfig.getKeyVaultType();

        List<Boolean> outcomes = new ArrayList<>();
        if (keyVaultType == KeyVaultType.AZURE) {

            if (!keyVaultConfig.getProperties().containsKey("url")) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("URL is required")
                        .addConstraintViolation();
                outcomes.add(Boolean.FALSE);
            }
        }

        if (keyVaultType == KeyVaultType.HASHICORP) {

            if (!keyVaultConfig.getProperties().containsKey("url")) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("URL is required")
                        .addConstraintViolation();
                outcomes.add(Boolean.FALSE);
            }

            PathValidator pathValidator = new PathValidator();
            ValidPath validPath = this.getClass().getAnnotation(ValidPath.class);
            pathValidator.initialize(validPath);

            if (keyVaultConfig.getProperties().containsKey("tlsKeyStorePath")) {
                Path tlsKeyStorePath = Paths.get(keyVaultConfig.getProperties().get("tlsKeyStorePath"));
                outcomes.add(pathValidator.isValid(tlsKeyStorePath, constraintValidatorContext));
            }
            if (keyVaultConfig.getProperties().containsKey("tlsTrustStorePath")) {
                Path tlsKeyStorePath = Paths.get(keyVaultConfig.getProperties().get("tlsTrustStorePath"));
                outcomes.add(pathValidator.isValid(tlsKeyStorePath, constraintValidatorContext));
            }
        }

        return outcomes.stream().allMatch(Boolean::booleanValue);
    }
}
