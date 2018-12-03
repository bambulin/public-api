package io.whalebone.publicapi.rest.validation;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.ext.Provider;

@Provider
public class RangedIntegerValidator implements ConstraintValidator<RangedInteger, String> {
    private int min;
    private int max;

    @Override
    public void initialize(RangedInteger constraint) {
        min = constraint.min();
        max = constraint.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            int number = java.lang.Integer.parseInt(value);
            return number >= min && number <= max;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
