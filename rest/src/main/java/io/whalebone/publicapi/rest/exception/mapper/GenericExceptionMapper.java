package io.whalebone.publicapi.rest.exception.mapper;

import io.whalebone.publicapi.rest.exception.EAppError;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final String ADMIN_EMAIL = System.getenv().containsKey("ADMIN_EMAIL") ? System.getenv("ADMIN_EMAIL") : null;

    @Inject
    private Logger logger;

    @Override
    public Response toResponse(Throwable throwable) {
        String message = getMessage();
        logger.log(Level.SEVERE, message, throwable);
        return EAppError.UNEXPECTED_ERROR.toResponseWithMessage(message);
    }

    public static String getMessage() {
        String ref = UUID.randomUUID().toString();
        return String.format("Unexpected error occurred (ref: %s). Please note the 'ref' number and time and contact " +
                "the administrator%s", ref, StringUtils.isNotBlank(ADMIN_EMAIL) ? "(" + ADMIN_EMAIL + ")" : "");
    }
}
