package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class KeyConfigurationValidator
    implements ConstraintValidator<ValidKeyConfiguration, KeyConfiguration> {

  @Override
  public boolean isValid(KeyConfiguration keyConfiguration, ConstraintValidatorContext cvc) {

    return keyConfiguration == null
        || !(keyConfiguration.getPasswordFile() != null && keyConfiguration.getPasswords() != null);
  }
}
