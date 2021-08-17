package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.ConfigItem;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class NoUnmatchedElementsValidatorTest {

  private NoUnmatchedElementsValidator validator;

  private ConstraintValidatorContext cvc;

  private NoUnmatchedElements config;

  @Before
  public void setUp() {
    validator = new NoUnmatchedElementsValidator();
    cvc = mock(ConstraintValidatorContext.class);
    config = mock(NoUnmatchedElements.class);
    validator.initialize(config);
  }

  @Test
  public void isValid() {
    ConfigItem configItem = mock(ConfigItem.class);
    Element dummy = mock(Element.class);
    when(configItem.getUnmatched()).thenReturn(Collections.singletonList(dummy));

    assertThat(validator.isValid(configItem, cvc)).isTrue();
  }
}
