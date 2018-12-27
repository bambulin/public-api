package io.whalebone.publicapi.rest.exception.mapper;

import com.google.gson.Gson;
import io.whalebone.publicapi.rest.Api;
import io.whalebone.publicapi.rest.EnumParamUtils;
import io.whalebone.publicapi.rest.exception.EAppError;
import io.whalebone.publicapi.rest.exception.dto.AppErrorMessage;
import io.whalebone.publicapi.rest.exception.dto.InvalidEnumValueErrorMessage;
import io.whalebone.publicapi.rest.exception.dto.MultipleAppErrorMessage;
import io.whalebone.publicapi.rest.exception.dto.ParameterValidationErrorMessage;
import io.whalebone.publicapi.rest.validation.EnumValue;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        MultipleAppErrorMessage error = new MultipleAppErrorMessage("Request validation failed");
        for (ConstraintViolation v : e.getConstraintViolations()) {
            error.addError(mapViolationToError(v));
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .encoding(StandardCharsets.UTF_8.name())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error)
                .build();
    }

    private static AppErrorMessage mapViolationToError(ConstraintViolation violation) {
        if (violation.getPropertyPath() != null && violation.getLeafBean() != null) {
            String queryParamName = getQueryParamName(violation.getLeafBean().getClass(), violation.getPropertyPath());
            if (violation.getConstraintDescriptor().getAnnotation() instanceof EnumValue) {
                EnumValue enumConstraint = (EnumValue) violation.getConstraintDescriptor().getAnnotation();
                Class<? extends Enum<?>> enumClass = enumConstraint.value();
                List<String> enumConstants = EnumParamUtils.getEnumValuesForParam(enumClass);
                return new InvalidEnumValueErrorMessage(EAppError.INVALID_PARAM_VALUE, violation.getMessage(),
                        queryParamName, violation.getInvalidValue(), enumConstants);
            } else {
                return new ParameterValidationErrorMessage(EAppError.INVALID_PARAM_VALUE, violation.getMessage(), queryParamName, violation.getInvalidValue());
            }
        }
        return new AppErrorMessage(EAppError.CONSTRAINT_VIOLATION, violation.getMessage());
    }

    private static String getQueryParamName(Class beanClass, Path propertyPath) {
        String fieldName = propertyPath.toString();
        Field field = FieldUtils.getField(beanClass, fieldName, true);
        if (field != null) {
            QueryParam a = field.getAnnotation(QueryParam.class);
            return a != null ? a.value() : fieldName;
        }
        return fieldName;
    }
}
