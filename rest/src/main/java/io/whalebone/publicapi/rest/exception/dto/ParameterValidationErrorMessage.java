package io.whalebone.publicapi.rest.exception.dto;

import io.whalebone.publicapi.rest.exception.EAppError;

public class ParameterValidationErrorMessage extends AppErrorMessage {
    private String parameter;
    private Object value;

    public ParameterValidationErrorMessage(EAppError error, String message, String parameter, Object value) {
        super(error, message);
        this.parameter = parameter;
        this.value = value;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
