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
        String token = "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9." +
                "eyJjbGllbnRfaWQiOiJLbE54anJnV3NFS3ZMa2pFaXlXVHFRPT0iLCJpYXQiOjE1MTYyMzkwMjJ9." +
                "IHj9Sw-BNOwjRLSnJH2mz64kRtjoQZRqlgA2Ts9pDomhpBWoxLq0cSocLpE7exSzJZhU0__sKiw-AaIYQ4RGtA";
        MultivaluedMap<String, String> headers = createHeaders(token);
        when(ctx.getHeaders()).thenReturn(headers);

        intcptr.filter(ctx);

        verify(ctx, times(2)).getHeaders();
        verifyNoMoreInteractions(ctx);
        assertThat(headers.get("wb_client_id"), hasSize(1));
        assertThat(headers.get("wb_client_id").get(0), is("2"));
    }

    @Test
    public void filter_missingToken() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        when(ctx.getHeaders()).thenReturn(headers);

        intcptr.filter(ctx);

        verify(ctx).getHeaders();
        verify(ctx).abortWith(responseCaptor.capture());
        verifyNoMoreInteractions(ctx);
        assertThat(headers.size(), is(0));
        assertThat(responseCaptor.getValue().getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
        AppErrorMessage error = (AppErrorMessage) responseCaptor.getValue().getEntity();
        assertThat(error.getError(), is(EAppError.MISSING_AUTH_TOKEN));
        assertThat(error.getMessage(), containsStringIgnoringCase("missing"));
    }

    @DataProvider
    public Object[] filter_invalidToken_testData() {
        return new Object[] {
                "", " ", "    ", "Bearer", "Bearer ", "Bearer     ",
                "Bearer blabla",
                // client id is empty string
                "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRfaWQiOiIiLCJpYXQiOjE1MTYyMzkwMjJ9.DJLfzHJtVMYVXmOHjQxncpscthOIdCrpCPEyVdm9s8R0TquuFNz57TT_4FKKK2cH-t1FeCBWwjG8JFC3usygJw",
                // client id is not string
                "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRfaWQiOjEsImlhdCI6MTUxNjIzOTAyMn0.5ln8vYOUbNWtobXV0pSVQsm9-Enua40htTezDPeHlAINNXY3xKicb3M8uzvjQ-YGitIuSVtSGDr5JcA6P3rG8A",
                // client id field missing at all
                "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE1MTYyMzkwMjJ9.iU-nmd099Db2jCgnyDA-wNytc-_bJE0DZLBBckTmsI6mDEAfZm03QelBx8UomeDo3E3X2_KZy0l8BqJNbTJoyA",
                // client id encrypted badly
                "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRfaWQiOiI1NGlqY0hoMTBLTTRONWZMTHZndllnPT0iLCJpYXQiOjE1MTYyMzkwMjJ9.AGOoaMAu0JlnwHRUqOWmp-BBzstPs0BUrpcrRy-MOKOc49JaItpGaoifv729tOtdHgmIHOG4L-oTJqezKizLtQ"
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
        assertThat(headers.size(), is(1));
        assertThat(responseCaptor.getValue().getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
        AppErrorMessage error = (AppErrorMessage) responseCaptor.getValue().getEntity();
        assertThat(error.getError(), is(EAppError.INVALID_AUTH_TOKEN));
        assertThat(error.getMessage(), containsString("invalid"));
    }

    @Test
    public void filter_verificationFailed() throws Exception {
        String token = "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9." +
                "eyJjbGllbnRfaWQiOiIyIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                "9nLXOIaDJfw03YJnPZWi6dnRvlr4Rk6136TQMiVniKX7c0yo9PWV58Qyq_B5tObBU2gyxnZDVJhlBHEe99JPhg";
        MultivaluedMap<String, String> headers = createHeaders(token);
        when(ctx.getHeaders()).thenReturn(headers);

        intcptr.filter(ctx);

        verify(ctx).getHeaders();
        verify(ctx).abortWith(responseCaptor.capture());
        verifyNoMoreInteractions(ctx);
        assertThat(headers.size(), is(1));
        assertThat(responseCaptor.getValue().getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
        AppErrorMessage error = (AppErrorMessage) responseCaptor.getValue().getEntity();
        assertThat(error.getError(), is(EAppError.INVALID_AUTH_TOKEN));
        assertThat(error.getMessage(), containsString("failed"));
    }

    private MultivaluedMap<String, String> createHeaders(String authHeader) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("Authorization", authHeader);
        return headers;
    }
}
