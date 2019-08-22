package io.whalebone.publicapi.rest.auth;

import io.whalebone.publicapi.rest.exception.EAppError;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.logging.Logger;

@Provider
@Priority(1)
public class AuthInterceptor implements ContainerRequestFilter {
    public static final String CLIENT_ID_HEADER = "Wb-Client-Id";

    @Inject
    private Logger logger;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        List<String> clientIdHeaders = requestContext.getHeaders().get(CLIENT_ID_HEADER);
        if (CollectionUtils.isEmpty(clientIdHeaders) || StringUtils.isBlank(clientIdHeaders.get(0))) {
            requestContext.abortWith(EAppError.MISSING_CLIENT_ID_HEADER.toResponseWithMessage("Missing or empty " + CLIENT_ID_HEADER + " header"));
        }
    }
}
