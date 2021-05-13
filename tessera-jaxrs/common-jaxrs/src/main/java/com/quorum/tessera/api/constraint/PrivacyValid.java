package com.quorum.tessera.api.constraint;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import javax.validation.Constraint;
import javax.validation.Payload;

@Retention(RUNTIME)
@Constraint(validatedBy = RequestPrivacyValidator.class)
public @interface PrivacyValid {

  String message() default "Send request not valid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
