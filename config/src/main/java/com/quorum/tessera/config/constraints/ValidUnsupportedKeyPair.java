package com.quorum.tessera.config.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = UnsupportedKeyPairValidator.class)
@Documented
public @interface ValidUnsupportedKeyPair {

  String message() default "{UnsupportedKeyPair.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
