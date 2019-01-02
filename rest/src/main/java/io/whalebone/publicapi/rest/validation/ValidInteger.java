package io.whalebone.publicapi.rest.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidIntegerValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInteger {
    String message() default "Invalid value - value must be an integer";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
