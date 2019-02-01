package io.whalebone.publicapi.rest.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.whalebone.publicapi.rest.exception.EAppError;
import io.whalebone.publicapi.rest.exception.mapper.GenericExceptionMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class AuthInterceptor implements ContainerRequestFilter {
    private static final String SECRET = System.getenv("JWT_SECRET");
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_SCHEME = "Bearer";
    private static final String CLIENT_ID = "wb_client_id";

    @Inject
    private Logger logger;

    private JWTVerifier verifier;

    public AuthInterceptor() {
        if (StringUtils.isBlank(SECRET)) {
            throw new IllegalStateException("Env prop JWT_SECRET is not set");
        }
        Algorithm algorithm = Algorithm.HMAC512(SECRET);
        verifier = JWT.require(algorithm).build();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        List<String> authorization = requestContext.getHeaders().get(AUTH_HEADER);
        if (CollectionUtils.isEmpty(authorization)) {
            requestContext.abortWith(EAppError.MISSING_AUTH_TOKEN.toResponseWithMessage("Missing auth token"));
            return;
        }
        try {
            String jwtToken = authorization.get(0).replaceFirst(AUTH_SCHEME + " ", "");
            DecodedJWT decoded = verifier.verify(jwtToken);
            String clientIdEncrypted = decoded.getClaim("client_id").asString();
            if (StringUtils.isBlank(clientIdEncrypted)) {
                requestContext.abortWith(EAppError.INVALID_AUTH_TOKEN.toResponseWithMessage("Token is invalid"));
                return;
            }
            String clientId = AESDecryptor.decrypt(clientIdEncrypted);
            requestContext.getHeaders().add(CLIENT_ID, clientId);
        } catch (JWTDecodeException | DecryptionException jde) {
            requestContext.abortWith(EAppError.INVALID_AUTH_TOKEN.toResponseWithMessage("Token is invalid"));
        } catch (JWTVerificationException jwe) {
            requestContext.abortWith(EAppError.INVALID_AUTH_TOKEN.toResponseWithMessage("Token verification failed"));
        } catch (Throwable t) {
            String message = GenericExceptionMapper.getMessage();
            logger.log(Level.SEVERE, message, t);
            requestContext.abortWith(EAppError.UNEXPECTED_ERROR.toResponseWithMessage(message));
        }
    }
}
