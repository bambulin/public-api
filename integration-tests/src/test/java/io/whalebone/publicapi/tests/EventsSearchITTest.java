package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.tests.utils.InvalidRequestUtils;
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

import static io.whalebone.publicapi.tests.matchers.EventMatcher.event;
import static io.whalebone.publicapi.tests.matchers.ParamValidationErrorMatcher.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.fail;

public class EventsSearchITTest extends Arquillian {
    private static final String CLIENT_ID = "2";

    private ArchiveInitiator archiveInitiator;

    public EventsSearchITTest() {
        archiveInitiator = new ArchiveInitiator();
    }

    @BeforeMethod
    public void setUp() throws IOException {
        archiveInitiator.cleanEventLogs();
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByClientIpTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/by_client_ip", timestamp);
        archiveInitiator.sendLogEvent("logs/by_client_ip/outdated/log-client_ip-1.2.3.4_c_outdated.json", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray events = eventsSearch(context, "client_ip=1.2.3.4");
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
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(eventA, is(event(timestampFormatted, 56, 1, "block", null, "1.2.3.4", "a.net", new String[] {"c&c"},
                new String[] {"Tinba"}, null, null, null)));
        assertThat(eventB, is(event(timestampFormatted, null, 2, "log", "legal", "1.2.3.4", "stoppblock.org",
                new String[] {"malware", "c&c", "legal"}, new String[] {"identifier1", "identifier2", "identifier3"},
                41.8776, -87.6272, "US")));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByClientIpWithWildCardTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/by_client_ip_wildcard", timestamp);
        archiveInitiator.sendLogEvent("logs/by_client_ip_wildcard/outdated/log-client_ip-1.2.3.4_outdated.json", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray events = eventsSearch(context, "client_ip=1.2.3.*");
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
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(eventA, is(event(timestampFormatted, 56, 1, "block", null, "1.2.3.4", "a.net", new String[] {"c&c"},
                new String[] {"Tinba"}, null, null, null)));
        assertThat(eventB, is(event(timestampFormatted, null, 2, "log", "legal", "1.2.3.5", "stoppblock.org", new String[] {"malware", "c&c"},
                new String[] {"identifier1", "identifier2", "identifier3"}, 41.8776, -87.6272, "US")));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByThreatTypeTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/by_threat_type", timestamp);
        archiveInitiator.sendLogEvent("logs/by_threat_type/outdated/log-threat_type-c_and_c-c-outdated.json", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray events = eventsSearch(context, "threat_type=c%26c");
        assertThat(events.size(), is(2));
        JsonElement eventA;
        JsonElement eventB;
        if (events.get(0).getAsJsonObject().get("resolver_id").getAsInt() == 42) {
            eventA = events.get(0);
            eventB = events.get(1);
        } else {
            eventA = events.get(1);
            eventB = events.get(0);
        }
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(eventA, is(event(timestampFormatted, 1, 42, "block", null, "1.2.3.1", "a.net", new String[] {"c&c"},
                new String[] {"Tinba"}, null, null, null)));
        assertThat(eventB, is(event(timestampFormatted, null, 43, "some_action", "content", "1.2.3.2", "stoppblock.org", new String[] {"malware", "c&c"},
                new String[] {"identifier1", "identifier2", "identifier3"}, 41.8776, -87.6272, "US")));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByReasonTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/by_reason", timestamp);
        archiveInitiator.sendLogEvent("logs/by_reason/outdated/log-reason-legal-outdated.json", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray events = eventsSearch(context, "reason=legal");
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
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(eventA, is(event(timestampFormatted, 56, 1, "block", "legal", "1.2.3.4", "a.net", new String[] {"c&c"},
                new String[] {"Tinba"}, null, null, null)));
        assertThat(eventB, is(event(timestampFormatted, null, 2, "log", "legal", "1.2.3.5", "stoppblock.org", new String[] {"malware", "c&c"},
                new String[] {"identifier1", "identifier2", "identifier3"}, 41.8776, -87.6272, "US")));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByResolverIdTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/by_resolver_id", timestamp);
        archiveInitiator.sendLogEvent("logs/by_resolver_id/outdated/log-resolver_id-42-outdated.json", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray events = eventsSearch(context, "resolver_id=42");
        assertThat(events.size(), is(2));
        JsonElement eventA;
        JsonElement eventB;
        if ("block".equals(events.get(0).getAsJsonObject().get("action").getAsString())) {
            eventA = events.get(0);
            eventB = events.get(1);
        } else {
            eventA = events.get(1);
            eventB = events.get(0);
        }
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(eventA, is(event(timestampFormatted, 56, 42, "block", "legal", "1.2.3.4", "a.net", new String[] {"c&c"},
                new String[] {"Tinba"}, null, null, null)));
        assertThat(eventB, is(event(timestampFormatted, null, 42, "log", "legal", "1.2.3.5", "stoppblock.org", new String[] {"malware", "c&c"},
                new String[] {"identifier1", "identifier2", "identifier3"}, 41.8776, -87.6272, "US")));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByDomainTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/by_domain", timestamp);
        archiveInitiator.sendLogEvent("logs/by_domain/outdated/log-domain-whalebone.io-outdated.json", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray events = eventsSearch(context, "domain=whalebone.io");
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
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(eventA, is(event(timestampFormatted, 56, 1, "block", "legal", "1.2.3.4", "whalebone.io", new String[] {"c&c"},
                new String[] {"Tinba"}, null, null, null)));
        assertThat(eventB, is(event(timestampFormatted, null, 2, "log", "legal", "1.2.3.5", "whalebone.io", new String[] {"malware", "c&c"},
                new String[] {"identifier1", "identifier2", "identifier3"}, 41.8776, -87.6272, "US")));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByDomainWithWildcardTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/by_domain_wildcard/", timestamp);
        archiveInitiator.sendLogEvent("logs/by_domain_wildcard/outdated/log-domain-whalebone.io-outdated.json", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray events = eventsSearch(context, "domain=whale*");
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
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(eventA, is(event(timestampFormatted, 56, 1, "block", "legal", "1.2.3.4", "whalebone.io", new String[] {"c&c"},
                new String[] {"Tinba"}, null, null, null)));
        assertThat(eventB, is(event(timestampFormatted, null, 2, "log", "legal", "1.2.3.5", "whalemouth.org", new String[] {"malware", "c&c"},
                new String[] {"identifier1", "identifier2", "identifier3"}, 41.8776, -87.6272, "US")));
    }

    /**
     * verifies if query can return more than 10 records (which is elastic limit by default)
     *
     * @param context
     * @throws IOException
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchQuerySizeTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/query-size", timestamp);
        JsonArray events = eventsSearch(context, "");
        assertThat(events.size(), is(11));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchByParamsCombinationTest(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        ZonedDateTime yesterday = timestamp.minusMinutes(24 * 60 + 1);
        archiveInitiator.sendMultipleLogEvents("logs/by_params_combination/", timestamp);
        archiveInitiator.sendMultipleLogEvents("logs/by_params_combination/yesterday", yesterday);
        archiveInitiator.sendLogEvent("logs/by_params_combination/outdated/log-all-params-match-outdated.json", timestamp.minusMinutes(2 * 24 * 60 + 1));
        JsonArray events = eventsSearch(context, "resolver_id=42&client_ip=1.2.3.4&reason=legal&threat_type=malware&domain=whalebone.io&days=2");
        assertThat(events.size(), is(1));
        JsonElement event = events.get(0).getAsJsonObject();
        String timestampFormatted = yesterday.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        assertThat(event, is(event(timestampFormatted, 56, 42, "log", "legal", "1.2.3.4", "whalebone.io", new String[] {"malware", "c&c"},
                new String[] {"identifier1", "identifier2", "identifier3"}, 41.8776, -87.6272, "US")));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchNoParameters(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleLogEvents("logs/by_no_params", timestamp);
        archiveInitiator.sendLogEvent("logs/by_no_params/outdated/log-outdated.json", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray events = eventsSearch(context, "");
        assertThat(events.size(), is(4));
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern(PublicApiService.TIME_PATTERN));
        for (JsonElement eventElement : events) {
            JsonObject event = eventElement.getAsJsonObject();
            int resolverId = event.get("resolver_id").getAsInt();
            switch (resolverId) {
                case 1:
                    assertThat(event, is(event(timestampFormatted, 1, 1, "log", "blacklist", "1.2.3.4", "stoppblock.org", new String[] {"c&c"},
                            new String[] {"identifier1"}, 1.8776, -1.6272, "US")));
                    break;
                case 2:
                    assertThat(event, is(event(timestampFormatted, 2, 2, "block", "content", "1.2.3.5", "whalebone.io", new String[] {"blacklist"},
                            new String[] {"identifier2"}, 2.8776, -2.6272, "DE")));
                    break;
                case 3:
                    assertThat(event, is(event(timestampFormatted, 3, 3, "action", "legal", "1.2.3.6", "virus.com", new String[] {"malware"},
                            new String[] {"identifier3"}, 3.8776, -3.6272, "GB")));
                    break;
                case 4:
                    assertThat(event, is(event(timestampFormatted, 4, 4, "some_action", "accuracy", "1.2.3.7", "phishing.org", new String[] {"phishing"},
                            new String[] {"identifier4"}, 4.8776, -4.6272, "RU")));
                    break;
                default:
                    fail("Unexpected event returned: " + event);
            }
        }
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchInvalidResolverId(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = eventsSearchInvalid(context, "resolver_id=abc");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("resolver_id", "abc", 21, "INVALID_PARAM_VALUE",
                "Invalid value - value must be an integer", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchInvalidThreatType(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = eventsSearchInvalid(context, "threat_type=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("threat_type", "xyz", 21, "INVALID_PARAM_VALUE", "Invalid enum value",
                new String[] {"c\u0026c", "blacklist", "malware", "phishing", "exploit", "legal"})));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchInvalidReason(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = eventsSearchInvalid(context, "reason=blabla");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("reason", "blabla", 21, "INVALID_PARAM_VALUE", "Invalid enum value",
                new String[] {"legal", "content", "accuracy", "blacklist"})));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchInvalidDays(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = eventsSearchInvalid(context, "days=noInt");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "noInt", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 90>", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchMinDays(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = eventsSearchInvalid(context, "days=0");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "0", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 90>", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchMaxDays(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = eventsSearchInvalid(context, "days=91");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "91", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 90>", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventsSearchMultipleMonths(@ArquillianResource URL context) throws IOException {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendLogEvent("logs/multiple_month_indices/log-client_ip-1.2.3.4.json", timestamp.minusMonths(1));
        archiveInitiator.sendLogEvent("logs/multiple_month_indices/log-client_ip-1.2.3.4.json", timestamp.minusMonths(2));
        // timestamp.minusMonths(3) cannot be used because it would be more than 90 days which is max possible value for search
        archiveInitiator.sendLogEvent("logs/multiple_month_indices/log-client_ip-1.2.3.4.json", timestamp.minusDays(80));
        JsonArray events = eventsSearch(context, "days=90");
        assertThat(events.size(), is(3));
    }

    private static JsonArray eventsSearch(URL context, String queryString) throws IOException {
        WebClient webClient = new WebClient();
        WebRequest requestSettings = new WebRequest(new URL(context + "1/events/search?" + queryString), HttpMethod.GET);
        requestSettings.setAdditionalHeader("WB-Client-Id", CLIENT_ID);
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertThat(page.getWebResponse().getStatusCode(), is(HttpURLConnection.HTTP_OK));
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(page.getWebResponse().getContentAsString());
        return element.getAsJsonArray();
    }

    private static JsonArray eventsSearchInvalid(URL context, String queryString) throws IOException {
        return InvalidRequestUtils.sendInvalidRequest(context, "1/events/search?" + queryString, CLIENT_ID);
    }

    /**
     * returns timestamp that would be used in testing data
     * 1 min minus to fit in search time range filter
     */
    private ZonedDateTime timestamp() {
        return ZonedDateTime.now().minusMinutes(1);
    }
}
