package io.whalebone.publicapi.rest.exception.mapper;

import com.google.gson.Gson;
import io.whalebone.publicapi.rest.Api;
import io.whalebone.publicapi.rest.exception.AppException;
import io.whalebone.publicapi.rest.exception.dto.AppErrorMessage;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {
    @Inject
    private Logger logger;

    @Override
    public Response toResponse(AppException exception) {
        logger.log(Level.SEVERE, "Application exception occurred", exception);
        return Response.status(exception.getError().getStatus())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .encoding(StandardCharsets.UTF_8.name())
                .entity(new AppErrorMessage(exception.getError(), exception.getMessage()))
                .build();
    }
}
