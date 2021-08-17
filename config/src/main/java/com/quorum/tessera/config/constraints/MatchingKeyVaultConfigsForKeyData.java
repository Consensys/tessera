package com.quorum.tessera.config.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD, PARAMETER, ANNOTATION_TYPE, TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = MatchingKeyVaultConfigsForKeyDataValidator.class)
@Documented
public @interface MatchingKeyVaultConfigsForKeyData {

  String message() default "{MatchingKeyVaultConfigsForKeyData.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
