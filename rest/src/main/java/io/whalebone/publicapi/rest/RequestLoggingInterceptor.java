package io.whalebone.publicapi.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Provider
@Priority(2) //executes after the AuthInterceptor
public class RequestLoggingInterceptor implements ContainerRequestFilter {
    protected static final String LOG_PREFIX = "WB API Request: ";
    private static final String SECRET_KEY_HEADER = "Wb-Secret-Key";
    @Inject
    private Logger logger;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Gson gson = new GsonBuilder().create();
        RequestLogRecord record = RequestLogRecord.builder()
                .method(requestContext.getMethod())
                .uri(requestContext.getUriInfo().getRequestUri().toString())
                .body(getBody(requestContext))
                .headers(maskedHeaders(requestContext))
                .build();
        String loggedRequest = gson.toJson(record);
        logger.log(Level.INFO, LOG_PREFIX + loggedRequest);
    }

    private String getBody(ContainerRequestContext context) {
        if (context.hasEntity()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getEntityStream(), StandardCharsets.UTF_8))) {
                String body = br.lines().collect(Collectors.joining(System.lineSeparator()));
                // put the input stream back to request so it can be processed later
                context.setEntityStream(new ByteArrayInputStream(body.getBytes()));
                return body;
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Cannot read request body", ioe);
            }
        }
        return "";
    }

    private static MultivaluedMap<String, String> maskedHeaders(ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        if (CollectionUtils.isNotEmpty(headers.get(SECRET_KEY_HEADER))) {
            headers.put(SECRET_KEY_HEADER, Collections.singletonList("*****"));
        }
        return headers;
    }

    @Builder
    @Getter
    private static class RequestLogRecord {
        private String clientId;
        private String uri;
        private String method;
        private String body;
        private MultivaluedMap<String, String> headers;
    }
}
