package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.DefaultKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Define here to be used during path validation
@ValidPath(checkExists = true, message = "File does not exist")
public class KeyVaultConfigValidator
    implements ConstraintValidator<ValidKeyVaultConfig, DefaultKeyVaultConfig> {

  private ValidKeyVaultConfig config;

  private static final String URL = "url";
  private static final String TLS_KEY_STORE_PATH = "tlsKeyStorePath";
  private static final String TLS_TRUST_STORE_PATH = "tlsTrustStorePath";
  private static final String ENDPOINT = "endpoint";

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
            .buildConstraintViolationWithTemplate(String.format("%s: is required", URL))
            .addConstraintViolation();
        outcomes.add(Boolean.FALSE);
      }
    }

    if (keyVaultType == KeyVaultType.HASHICORP) {

      if (!keyVaultConfig.getProperties().containsKey(URL)) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
            .buildConstraintViolationWithTemplate(String.format("%s: is required", URL))
            .addConstraintViolation();
        outcomes.add(Boolean.FALSE);
      }

      final ValidPath validPath = this.getClass().getAnnotation(ValidPath.class);
      final PathValidator pathValidator = new PathValidator();
      pathValidator.initialize(validPath);

      Optional.ofNullable(keyVaultConfig.getProperties().get(TLS_KEY_STORE_PATH))
          .map(Paths::get)
          .filter(path -> !pathValidator.isValid(path, constraintValidatorContext))
          .ifPresent(
              b -> {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                        String.format("%s: %s", TLS_KEY_STORE_PATH, validPath.message()))
                    .addConstraintViolation();
                outcomes.add(Boolean.FALSE);
              });

      Optional.ofNullable(keyVaultConfig.getProperties().get(TLS_TRUST_STORE_PATH))
          .map(Paths::get)
          .filter(path -> !pathValidator.isValid(path, constraintValidatorContext))
          .ifPresent(
              b -> {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                        String.format("%s: %s", TLS_TRUST_STORE_PATH, validPath.message()))
                    .addConstraintViolation();
                outcomes.add(Boolean.FALSE);
              });
    }

    if (keyVaultType == KeyVaultType.AWS) {
      // we do not require endpoint to be provided as AWS client will fallback to alternate methods
      // (e.g. environment variables or properties files)
      Optional.ofNullable(keyVaultConfig.getProperties().get(ENDPOINT))
          .filter(endpoint -> !endpoint.matches("^https?://.+$"))
          .ifPresent(
              b -> {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                        String.format(
                            "%s: must be a valid AWS service endpoint URL with scheme", ENDPOINT))
                    .addConstraintViolation();
                outcomes.add(Boolean.FALSE);
              });
    }

    return outcomes.stream().allMatch(Boolean::booleanValue);
  }
}
