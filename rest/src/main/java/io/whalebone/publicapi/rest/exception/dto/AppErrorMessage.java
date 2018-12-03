package io.whalebone.publicapi.rest.exception.dto;

import io.whalebone.publicapi.rest.exception.EAppError;

public class AppErrorMessage {
    private EAppError error;
    private int errorCode;
    private String message;

    public AppErrorMessage(EAppError error, String message) {
        this.error = error;
        this.errorCode = error.getErrorCode();
        this.message = message;
    }

    public EAppError getError() {
        return error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
