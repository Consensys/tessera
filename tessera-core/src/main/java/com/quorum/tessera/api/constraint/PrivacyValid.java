package com.quorum.tessera.api.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = RequestPrivacyValidator.class)
public @interface PrivacyValid {

    String message()

        default "Send request not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
