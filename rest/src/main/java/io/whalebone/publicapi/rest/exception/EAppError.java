package io.whalebone.publicapi.rest.exception;

import javax.ws.rs.core.Response;

public enum EAppError {
    UNEXPECTED_ERROR(10, Response.Status.INTERNAL_SERVER_ERROR),
    CONSTRAINT_VIOLATION(20, Response.Status.BAD_REQUEST),
    INVALID_PARAM_VALUE(21, Response.Status.BAD_REQUEST);

    private int errorCode;
    private Response.Status status;

    EAppError(int errorCode, Response.Status status) {
        this.errorCode = errorCode;
        this.status = status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Response.Status getStatus() {
        return status;
    }
}
