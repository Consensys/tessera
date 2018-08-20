package com.quorum.tessera.config.constraints;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = Base64Validator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(value = ValidBase64.List.class)
public @interface ValidBase64 {

    String message() default "{ValidBase64.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target(value = {METHOD, FIELD, ANNOTATION_TYPE, PARAMETER, TYPE_USE})
    @Retention(value = RUNTIME)
    @Documented
    public @interface List {
        ValidBase64[] value();
    }

}
