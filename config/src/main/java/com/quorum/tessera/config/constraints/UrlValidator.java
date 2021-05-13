package com.quorum.tessera.config.constraints;

import java.net.MalformedURLException;
import java.net.URL;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UrlValidator implements ConstraintValidator<ValidUrl, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      new URL(value);
      return true;
    } catch (MalformedURLException e) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(String.format("Invalid URL: %s", e.getMessage()))
          .addConstraintViolation();
      return false;
    }
  }
}
