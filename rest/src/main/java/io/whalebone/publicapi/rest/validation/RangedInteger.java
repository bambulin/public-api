package io.whalebone.publicapi.rest.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RangedIntegerValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RangedInteger {
    String message() default "Invalid value - value must be an integer in range <{min} - {max}>";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int min();
    int max();
}
