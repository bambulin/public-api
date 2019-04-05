package io.whalebone.publicapi.rest;

import io.whalebone.publicapi.rest.auth.AuthInterceptor;
import lombok.Builder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Provider
@Priority(2) //executes after the AuthInterceptor
public class RequestLoggingInterceptor implements ContainerRequestFilter {
    @Inject
    private Logger logger;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        RequestLogRecord record = RequestLogRecord.builder()
                .method(requestContext.getMethod())
                .uri(requestContext.getUriInfo().getRequestUri().toString())
                .body(getBody(requestContext))
                .headers(requestContext.getHeaders())
                .build();
        logger.log(Level.INFO, record.toString());
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

    @Builder
    private static class RequestLogRecord {
        private String clientId;
        private String uri;
        private String method;
        private String body;
        private MultivaluedMap<String, String> headers;

        @Override
        public String toString() {
            return "WB API Request:\n" +
                    "\tmethod: " + method + "\n" +
                    "\turi: " + uri + "\n" +
                    "\theaders:\n" + printHeaders() +
                    "\tbody: " + body + "\n";
        }

        private String printHeaders() {
            StringBuilder headersSb = new StringBuilder();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                headersSb.append("\t\t").append(header.getKey()).append(": ");
                if (AuthInterceptor.AUTH_HEADER.equals(header.getKey())) {
                    if (CollectionUtils.isNotEmpty(header.getValue()) &&
                            StringUtils.isNotBlank(header.getValue().get(0))) {
                        headersSb.append(AuthInterceptor.AUTH_SCHEME + " <token>\n");
                    } else {
                        headersSb.append("\n");
                    }
                    continue;
                }
                List<String> headerValsList = header.getValue();
                if (CollectionUtils.isEmpty(headerValsList)) {
                    headersSb.append("\n");
                } else if (headerValsList.size() == 1) {
                    headersSb.append(headerValsList.get(0)).append("\n");
                } else {
                    headersSb.append("\n");
                    header.getValue().forEach(v -> headersSb.append("\t\t\t").append(v).append("\n"));
                }
            }
            return headersSb.toString();
        }
    }
}
