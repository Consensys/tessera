package com.quorum.tessera.config.constraints;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NoUnmatchedElementsValidator.class)
@Documented
public @interface NoUnmatchedElements {
  String message() default "{NoUnmatchedElements.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
