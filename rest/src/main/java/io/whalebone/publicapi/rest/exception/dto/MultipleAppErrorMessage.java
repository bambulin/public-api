package io.whalebone.publicapi.rest.exception.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MultipleAppErrorMessage implements Serializable {
    private String message;
    private List<AppErrorMessage> errors;

    public MultipleAppErrorMessage(String message) {
        this.message = message;
        errors = new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public List<AppErrorMessage> getErrors() {
        return errors;
    }

    public void addError(AppErrorMessage error) {
        errors.add(error);
    }
}
