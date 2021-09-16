package com.quorum.tessera.config.constraints;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.KeyConfiguration;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.Paths;
import org.junit.Test;

public class KeyConfigurationValidatorTest {

  private KeyConfigurationValidator validator = new KeyConfigurationValidator();

  @Test
  public void bothNotSetIsValid() {

    final KeyConfiguration configuration = new KeyConfiguration(null, null, null, null, null);

    assertThat(validator.isValid(configuration, mock(ConstraintValidatorContext.class))).isTrue();
  }

  @Test
  public void fileSetIsValid() {

    final KeyConfiguration configuration =
        new KeyConfiguration(Paths.get("anything"), null, null, null, null);

    assertThat(validator.isValid(configuration, mock(ConstraintValidatorContext.class))).isTrue();
  }

  @Test
  public void inlineSetIsValid() {

    final KeyConfiguration configuration =
        new KeyConfiguration(null, emptyList(), null, null, null);

    assertThat(validator.isValid(configuration, mock(ConstraintValidatorContext.class))).isTrue();
  }

  @Test
  public void bothSetIsInvalid() {

    final KeyConfiguration configuration =
        new KeyConfiguration(Paths.get("anything"), emptyList(), null, null, null);

    assertThat(validator.isValid(configuration, mock(ConstraintValidatorContext.class))).isFalse();
  }

  @Test
  public void nullConfigIsValid() {
    assertThat(validator.isValid(null, mock(ConstraintValidatorContext.class))).isTrue();
  }
}
