package com.quorum.tessera.config.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({FIELD, PARAMETER, ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = AzureVaultKeyPairValidator.class)
@Documented
public @interface ValidAzureVaultKeyPair {

  String message() default "{AzureVaultKeyData.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
