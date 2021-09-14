package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.ConfigItem;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class NoUnmatchedElementsValidator
    implements ConstraintValidator<NoUnmatchedElements, ConfigItem> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NoUnmatchedElementsValidator.class);

  private NoUnmatchedElements config;

  @Override
  public void initialize(NoUnmatchedElements config) {
    this.config = config;
  }

  @Override
  public boolean isValid(
      ConfigItem configItem, ConstraintValidatorContext constraintValidatorContext) {
    Optional.ofNullable(configItem.getUnmatched())
        .ifPresent(
            list -> {
              list.forEach(
                  e -> {
                    if (Element.class.isAssignableFrom(e.getClass())) {
                      Element element = Element.class.cast(e);
                      LOGGER.warn(
                          "Ignoring unknown/unmatched json element: {}", element.getTagName());
                    }
                  });
            });
    return true; // just log a warning, don't stop startup
  }
}
