package com.quorum.tessera.config.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = UnsupportedKeyPairValidator.class)
@Documented
public @interface ValidUnsupportedKeyPair {

    String message() default "{UnsupportedKeyPair.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
