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
@Constraint(validatedBy = ServerConfigsValidator.class)
@Documented
public @interface ValidServerConfigs {

  String message() default "{ValidServerConfigs.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
