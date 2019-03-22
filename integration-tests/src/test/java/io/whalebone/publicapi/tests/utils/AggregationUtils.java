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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AggregationUtils {
    public static JsonObject createBucketElement(String aggregate, String value, Integer count) {
        JsonObject element = new JsonObject();
        element.addProperty(aggregate, value);
        element.addProperty("count", count);
        return element;
    }

    /**
     * Returns aggregations corresponding to time from timeline
     * If there is none, an exception is thrown
     *
     * @param time
     * @param timeline
     * @return
     */
    public static JsonObject getTimeAggregation(ZonedDateTime time, JsonArray timeline) throws Exception {
        final String PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);

        ZonedDateTime timestamp = time.truncatedTo(ChronoUnit.HOURS);
        for (int i = 0; i < timeline.size(); i++) {
            String aggregationTimestampString = timeline.get(i).getAsJsonObject().get("timestamp").getAsString();
            ZonedDateTime aggregationTimestamp = ZonedDateTime.parse(aggregationTimestampString, formatter);
            if (timestamp.toInstant().equals(aggregationTimestamp.toInstant())) {
                return timeline.get(i).getAsJsonObject();
            }
        }
        throw new Exception("Expected timeline :" + timeline.toString() +
                " to contain timestamp: " + timestamp + " but it didn't.");
    }

    public static JsonArray getAggregationBucketsArray(URL context, String path, String token) throws IOException {
        WebClient webClient = new WebClient();
        WebRequest requestSettings = new WebRequest(new URL(context + path), HttpMethod.GET);
        requestSettings.setAdditionalHeader("Authorization", "Bearer " + token);
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertThat(page.getWebResponse().getStatusCode(), is(HttpURLConnection.HTTP_OK));
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(page.getWebResponse().getContentAsString());
        return element.getAsJsonArray();
    }
}
