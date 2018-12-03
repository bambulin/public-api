package io.whalebone.publicapi.rest.exception;

public class AppException extends Exception {
    private EAppError error;

    public AppException(String message, EAppError error) {
        super(message);
        this.error = error;
    }

    public AppException(String message, EAppError error, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public EAppError getError() {
        return error;
    }
}
