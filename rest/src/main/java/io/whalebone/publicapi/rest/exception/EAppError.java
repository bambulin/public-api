package io.whalebone.publicapi.rest.exception;

import io.whalebone.publicapi.rest.exception.dto.AppErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

public enum EAppError {
    MISSING_CLIENT_ID_HEADER(0, Response.Status.BAD_REQUEST),
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

    public Response toResponseWithMessage(String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .encoding(StandardCharsets.UTF_8.name())
                .entity(new AppErrorMessage(this, message))
                .build();
    }
}
