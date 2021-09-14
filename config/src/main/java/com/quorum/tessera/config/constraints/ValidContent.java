package com.quorum.tessera.config.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidContentValidator.class)
@Documented
public @interface ValidContent {

  String message() default "{ValidContent.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  int minLines() default 0;

  int maxLines() default Integer.MAX_VALUE;
}
