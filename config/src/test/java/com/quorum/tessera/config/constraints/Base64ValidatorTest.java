package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Base64;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Base64ValidatorTest {

  private Base64Validator validator = new Base64Validator();

  private ConstraintValidatorContext constraintValidatorContext;

  @Before
  public void onSetup() {
    this.constraintValidatorContext = mock(ConstraintValidatorContext.class);
  }

  @After
  public void onTearDown() {
    verifyZeroInteractions(constraintValidatorContext);
  }

  @Test
  public void validBase64() {
    final String value = Base64.getEncoder().encodeToString("HELLOW".getBytes());

    assertThat(validator.isValid(value, constraintValidatorContext)).isTrue();
  }

  @Test
  public void invalidBase64() {
    final String value = UUID.randomUUID().toString();

    assertThat(validator.isValid(value, constraintValidatorContext)).isFalse();
  }

  @Test
  public void nullValueIsIgnoredAndReturns() {
    assertThat(validator.isValid(null, constraintValidatorContext)).isTrue();
  }

  @Test
  public void naclFailureValueIsIgnoredAndReturns() {
    assertThat(validator.isValid("NACL_FAILURE: It's broken son!!", constraintValidatorContext))
        .isTrue();
    assertThat(validator.isValid("NACL_FAILURE", constraintValidatorContext)).isTrue();
  }
}
