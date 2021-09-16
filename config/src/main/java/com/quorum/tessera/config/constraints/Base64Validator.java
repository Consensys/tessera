package com.quorum.tessera.config.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Base64;
import java.util.Objects;

public class Base64Validator implements ConstraintValidator<ValidBase64, String> {

  private static final String NACL_FAILURE_TOKEN = "NACL_FAILURE";

  @Override
  public boolean isValid(String value, ConstraintValidatorContext cvc) {
    if (Objects.isNull(value)) {
      return true;
    }

    if (value.startsWith(NACL_FAILURE_TOKEN)) {
      return true;
    }

    try {
      Base64.getDecoder().decode(value);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
