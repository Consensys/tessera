
package com.quorum.tessera.config.constraints;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = KeyDataConfigValidator.class)
@Documented
public @interface ValidKeyDataConfig {
    
    
    String message() default "{ValidKeyDataConfig.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
