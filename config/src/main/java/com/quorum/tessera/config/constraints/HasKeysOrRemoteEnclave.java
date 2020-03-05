package com.quorum.tessera.config.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = HasKeysOrRemoteEnclaveValidator.class)
@Documented
public @interface HasKeysOrRemoteEnclave {

    String message() default "{HasKeysOrRemoteEnclave.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
