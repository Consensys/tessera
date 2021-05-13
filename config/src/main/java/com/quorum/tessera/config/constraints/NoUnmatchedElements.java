package com.quorum.tessera.config.constraints;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NoUnmatchedElementsValidator.class)
@Documented
public @interface NoUnmatchedElements {
  String message() default "{NoUnmatchedElements.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
