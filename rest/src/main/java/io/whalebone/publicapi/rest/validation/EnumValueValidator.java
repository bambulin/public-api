package io.whalebone.publicapi.rest.validation;

import io.whalebone.publicapi.rest.EnumParamUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        enumClass = constraintAnnotation.value();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.isBlank(value) || EnumParamUtils.getEnumValue((Class<? extends Enum>) enumClass, value) != null;
    }
}
