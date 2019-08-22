package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class AuthITTest extends Arquillian {

    // successful auth is tested by other IT tests

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidToken_eventsSearchEndpoint(@ArquillianResource URL context) throws Exception {
        assertInvalidTokenRequest(context + "1/events/search");
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidToken_dnsEndpoint(@ArquillianResource URL context) throws Exception {
        assertInvalidTokenRequest(context + "1/dns/timeline");
    }

    private void assertInvalidTokenRequest(String path) throws Exception {
        WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        WebRequest requestSettings = new WebRequest(new URL(path), HttpMethod.GET);
        // token is not signed properly
        requestSettings.setAdditionalHeader("Wb-Client-Id", "");
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);

        assertThat(page.getWebResponse().getStatusCode(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        JsonParser parser = new JsonParser();
        JsonElement responseJson = parser.parse(page.getWebResponse().getContentAsString());
        JsonObject errorResponse = responseJson.getAsJsonObject();
        assertThat(errorResponse.get("message"), is(notNullValue()));
        assertThat(errorResponse.get("error"), is(notNullValue()));
        assertThat(errorResponse.get("error").getAsString(), is("MISSING_CLIENT_ID_HEADER"));
    }
}
