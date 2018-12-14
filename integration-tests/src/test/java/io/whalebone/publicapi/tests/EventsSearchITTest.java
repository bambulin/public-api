package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.whalebone.publicapi.ejb.PublicApiService;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertEquals;

public class EventsSearchITTest extends Arquillian {

    private ArchiveInitiator archiveInitiator;

    public EventsSearchITTest() {
        archiveInitiator = new ArchiveInitiator();
    }

    @BeforeMethod
    public void prepare() throws IOException {
        archiveInitiator.cleanEventLogs();
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByClientIpTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleLogEvents("logs/by_client_ip", now);
        archiveInitiator.sendLogEvent("logs/by_client_ip/outdated/log-client_ip-1.2.3.4_c_outdated.json", now.minusMinutes(24 * 60 + 1));
        WebClient webClient = new WebClient();
        WebRequest requestSettings = new WebRequest(new URL(context + "1/events/search?client_ip=1.2.3.4"), HttpMethod.GET);
        requestSettings.setAdditionalHeader("whalebone_client_id", "2");
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(page.getWebResponse().getContentAsString());
        JsonArray events = element.getAsJsonArray();
        assertThat(events.size(), is(2));
        JsonElement eventA;
        JsonElement eventB;
        if (events.get(0).getAsJsonObject().get("resolver_id").getAsInt() == 1) {
            eventA = events.get(0);
            eventB = events.get(1);
        } else {
            eventA = events.get(1);
            eventB = events.get(0);
        }
        String timestamp = now.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(eventA, is(event(timestamp, 56, 1, "block", null, "1.2.3.4", "a.net", new String[] {"c&c"},
                new String[] {"Tinba"}, null, null, null)));
        assertThat(eventB, is(event(timestamp, null, 2, "log", "legal", "1.2.3.4", "stoppblock.org", new String[] {"malware", "c&c"},
                new String[] {"identifier1", "identifier2", "identifier3"}, 41.8776, -87.6272, "US")));
    }

    private static EventMatcher event(String timestamp,
                                      Integer accuracy,
                                      Integer resolverId,
                                      String action,
                                      String reason,
                                      String clientIp,
                                      String domain,
                                      String[] threatType,
                                      String[] identifier,
                                      Double latitude,
                                      Double longitude,
                                      String countryCode2) {
        return new EventMatcher(timestamp, accuracy, resolverId, action, reason, clientIp, domain, threatType,
                identifier, latitude, longitude, countryCode2);
    }
}
