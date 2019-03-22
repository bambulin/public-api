package io.whalebone.publicapi.tests.utils;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class InvalidRequestUtils {
    public static JsonArray sendInvalidRequest(URL context, String path, String token) throws IOException {
        WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        WebRequest requestSettings = new WebRequest(new URL(context + path), HttpMethod.GET);
        requestSettings.setAdditionalHeader("Authorization", "Bearer " + token);
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertThat(page.getWebResponse().getStatusCode(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        JsonParser parser = new JsonParser();
        JsonElement responseJson = parser.parse(page.getWebResponse().getContentAsString());
        JsonObject errorResponse = responseJson.getAsJsonObject();
        assertThat(errorResponse.get("message"), is(notNullValue()));
        assertThat(errorResponse.get("message").getAsString(), is(not(emptyString())));
        assertThat(errorResponse.get("errors"), is(notNullValue()));
        return errorResponse.get("errors").getAsJsonArray();
    }
}
