package io.whalebone.publicapi.rest;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.specimpl.MultivaluedTreeMap;
import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestLoggingInterceptorTest {

    @Mock
    private Logger logger;

    @Mock
    private ContainerRequestContext ctx;

    @Captor
    private ArgumentCaptor<String> logCaptor;

    @InjectMocks
    private RequestLoggingInterceptor interceptor;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @DataProvider
    public Object[][] logRecordTest_data() {
        return new Object[][] {
                {"GET", "http://host/end/point?query=string&param=value", createHeaders(), "", getExpectedRecord1()},
                {"POST", "http://host/end/point?query=string&param=value", createHeaders(), "SomeBody", getExpectedRecord2()}
        };
    }

    @Test(dataProvider = "logRecordTest_data")
    public void logRecordTest(String method, String uri, MultivaluedMap<String, String> headers, String body, String expectedLogRecord)
            throws Exception {
        when(ctx.getMethod()).thenReturn(method);
        UriInfo uriInfo = mock(UriInfo.class);
        URI uriObject = URI.create(uri);
        when(uriInfo.getRequestUri()).thenReturn(uriObject);
        when(ctx.getUriInfo()).thenReturn(uriInfo);
        when(ctx.getHeaders()).thenReturn(headers);
        if (StringUtils.isNotBlank(body)) {
            when(ctx.hasEntity()).thenReturn(true);
            when(ctx.getEntityStream()).thenReturn(new ByteArrayInputStream(body.getBytes()));
        }

        interceptor.filter(ctx);

        verify(logger).log(eq(Level.INFO), logCaptor.capture());
        assertThat(logCaptor.getValue(), is(expectedLogRecord));
    }

    private static MultivaluedMap<String, String> createHeaders() {
        MultivaluedMap<String, String> headers = new MultivaluedTreeMap<>(); // use treemap to ensure order of keys
        headers.add("SingleValueHeader", "headerVal1");
        headers.addAll("MultivalueHeader", "multival1", "multival2");
        headers.add("Authorization", "Bearer TOKEN_SHOULD_BE_OMITTED");
        headers.add("EmptyHeader", "");
        headers.add("NullHeader", null);
        return headers;
    }

    private static String getExpectedRecord1() {
        return "WB API Request:\n" +
                "\tmethod: GET\n" +
                "\turi: http://host/end/point?query=string&param=value\n" +
                "\theaders:\n" +
                "\t\tAuthorization: Bearer <token>\n" +
                "\t\tEmptyHeader: \n" +
                "\t\tMultivalueHeader: \n" +
                "\t\t\tmultival1\n" +
                "\t\t\tmultival2\n" +
                "\t\tNullHeader: null\n" +
                "\t\tSingleValueHeader: headerVal1\n" +
                "\tbody: \n";
    }

    private static String getExpectedRecord2() {
        return "WB API Request:\n" +
                "\tmethod: POST\n" +
                "\turi: http://host/end/point?query=string&param=value\n" +
                "\theaders:\n" +
                "\t\tAuthorization: Bearer <token>\n" +
                "\t\tEmptyHeader: \n" +
                "\t\tMultivalueHeader: \n" +
                "\t\t\tmultival1\n" +
                "\t\t\tmultival2\n" +
                "\t\tNullHeader: null\n" +
                "\t\tSingleValueHeader: headerVal1\n" +
                "\tbody: SomeBody\n";
    }
}
