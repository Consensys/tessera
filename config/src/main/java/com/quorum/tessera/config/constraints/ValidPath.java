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
@Constraint(validatedBy = PathValidator.class)
@Documented
public @interface ValidPath {

  String message() default "{ValidPath.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean checkExists() default false;

  boolean checkCanCreate() default false;
}
