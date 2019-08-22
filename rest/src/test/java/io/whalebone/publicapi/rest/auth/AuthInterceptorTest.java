package io.whalebone.publicapi.rest.auth;

import io.whalebone.publicapi.rest.exception.EAppError;
import io.whalebone.publicapi.rest.exception.dto.AppErrorMessage;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class AuthInterceptorTest {

    private AuthInterceptor intcptr;

    @Mock
    private ContainerRequestContext ctx;
    @Captor
    private ArgumentCaptor<Response> responseCaptor;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        intcptr = new AuthInterceptor();
    }

    @Test
    public void filter_success() throws Exception {
        MultivaluedMap<String, String> headers = createHeaders("clientId");
        when(ctx.getHeaders()).thenReturn(headers);

        intcptr.filter(ctx);

        verify(ctx).getHeaders();
        verifyNoMoreInteractions(ctx);
    }

    @Test
    public void filter_missingClientIdHeader() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        when(ctx.getHeaders()).thenReturn(headers);

        intcptr.filter(ctx);

        verify(ctx).getHeaders();
        verify(ctx).abortWith(responseCaptor.capture());
        verifyNoMoreInteractions(ctx);
        assertThat(responseCaptor.getValue().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        AppErrorMessage error = (AppErrorMessage) responseCaptor.getValue().getEntity();
        assertThat(error.getError(), is(EAppError.MISSING_CLIENT_ID_HEADER));
        assertThat(error.getMessage(), containsStringIgnoringCase("missing or empty"));
        assertThat(error.getMessage(), containsStringIgnoringCase(AuthInterceptor.CLIENT_ID_HEADER));
    }

    @DataProvider
    public Object[] filter_invalidToken_testData() {
        return new Object[] {
                null, "", " ", "    ",
        };
    }

    @Test(dataProvider = "filter_invalidToken_testData")
    public void filter_invalidToken(String token) {
        MultivaluedMap<String, String> headers = createHeaders(token);
        when(ctx.getHeaders()).thenReturn(headers);

        intcptr.filter(ctx);

        verify(ctx).getHeaders();
        verify(ctx).abortWith(responseCaptor.capture());
        verifyNoMoreInteractions(ctx);
        assertThat(responseCaptor.getValue().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        AppErrorMessage error = (AppErrorMessage) responseCaptor.getValue().getEntity();
        assertThat(error.getError(), is(EAppError.MISSING_CLIENT_ID_HEADER));
        assertThat(error.getMessage(), containsStringIgnoringCase("missing or empty"));
        assertThat(error.getMessage(), containsStringIgnoringCase(AuthInterceptor.CLIENT_ID_HEADER));
    }

    private MultivaluedMap<String, String> createHeaders(String clientId) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(AuthInterceptor.CLIENT_ID_HEADER, clientId);
        return headers;
    }
}
