package com.quorum.tessera.config.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = EitherServerConfigsOrServerValidator.class)
@Documented
public @interface ValidEitherServerConfigsOrServer {

    String message() default "{ValidEitherServerConfigsOrServer.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
