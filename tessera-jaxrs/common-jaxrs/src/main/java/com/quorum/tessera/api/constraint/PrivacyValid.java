package com.quorum.tessera.api.constraint;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;

@Retention(RUNTIME)
@Constraint(validatedBy = RequestPrivacyValidator.class)
public @interface PrivacyValid {

  String message() default "Send request not valid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
