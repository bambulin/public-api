package io.whalebone.publicapi.rest.exception.mapper;

import com.google.gson.Gson;
import io.whalebone.publicapi.rest.Api;
import io.whalebone.publicapi.rest.exception.EAppError;
import io.whalebone.publicapi.rest.exception.dto.AppErrorMessage;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
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
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .encoding(StandardCharsets.UTF_8.name())
                .entity(new AppErrorMessage(EAppError.UNEXPECTED_ERROR, message))
                .build();
    }

    private String getMessage() {
        String ref = UUID.randomUUID().toString();
        return String.format("Unexpected error occurred (ref: %s). Please note the 'ref' number and time and contact " +
                "the administrator%s", ref, StringUtils.isNotBlank(ADMIN_EMAIL) ? "(" + ADMIN_EMAIL + ")" : "");
    }
}
