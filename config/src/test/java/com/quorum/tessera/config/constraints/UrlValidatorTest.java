package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.Before;
import org.junit.Test;

public class UrlValidatorTest {

  private UrlValidator urlValidator;

  @Before
  public void setUp() {
    urlValidator = new UrlValidator();
  }

  @Test
  public void valid() {
    final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    final boolean result = urlValidator.isValid("http://validurl:8080", context);

    assertThat(result).isTrue();
    verifyNoMoreInteractions(context);
  }

  @Test
  public void invalid() {
    final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    final ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

    final boolean result = urlValidator.isValid("invalidurl", context);

    assertThat(result).isFalse();
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate("Invalid URL: no protocol: invalidurl");
    verifyNoMoreInteractions(context);
  }
}
