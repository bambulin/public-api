package io.whalebone.publicapi.rest.exception.dto;

import io.whalebone.publicapi.rest.exception.EAppError;

import java.util.List;

public class InvalidEnumValueErrorMessage extends ParameterValidationErrorMessage {
    private List<String> acceptedValues;

    public InvalidEnumValueErrorMessage(EAppError error, String message, String parameter, Object value, List<String> acceptedValues) {
        super(error, message, parameter, value);
        this.acceptedValues = acceptedValues;
    }

    public List<String> getAcceptedValues() {
        return acceptedValues;
    }
}
