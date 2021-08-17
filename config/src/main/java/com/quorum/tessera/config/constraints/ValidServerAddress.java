package com.quorum.tessera.config.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = ServerAddressValidator.class)
@Documented
public @interface ValidServerAddress {

  boolean isBindingAddress() default false;

  String[] supportedSchemes() default {"unix", "http", "https"};

  String message() default "{ValidServerAddress.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
