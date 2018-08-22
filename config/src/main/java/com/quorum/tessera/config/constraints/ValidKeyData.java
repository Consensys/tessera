package com.quorum.tessera.config.constraints;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = KeyDataValidator.class)
@Documented
@Repeatable(value = ValidKeyData.List.class)
public @interface ValidKeyData {

    String message() default "{ValidKeyData.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target(value = {METHOD, FIELD, ANNOTATION_TYPE, PARAMETER, TYPE_USE})
    @Retention(value = RUNTIME)
    @Documented
    public @interface List {
        ValidKeyData[] value();
    }
}
