package io.whalebone.publicapi.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import io.whalebone.publicapi.ejb.dto.aggregate.EDnsSecAggregate;
import io.whalebone.publicapi.tests.utils.AggregationUtils;
import io.whalebone.publicapi.tests.utils.InvalidRequestUtils;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static io.whalebone.publicapi.tests.matchers.ParamValidationErrorMatcher.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

public class DnsSecTimelineITTest extends Arquillian {
    private static final String TOKEN = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9." +
            "eyJjbGllbnRfaWQiOiJLbE54anJnV3NFS3ZMa2pFaXlXVHFRPT0iLCJpYXQiOjE1MTYyMzkwMjJ9." +
            "IHj9Sw-BNOwjRLSnJH2mz64kRtjoQZRqlgA2Ts9pDomhpBWoxLq0cSocLpE7exSzJZhU0__sKiw-AaIYQ4RGtA";

    private ArchiveInitiator archiveInitiator;

    public DnsSecTimelineITTest() {
        archiveInitiator = new ArchiveInitiator();
    }

    @BeforeMethod
    public void setUp() throws IOException {
        archiveInitiator.cleanDnsSecLogs();
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByDefaultTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/older", timestamp.minusHours(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/too_old", timestamp.minusDays(1).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "");

        assertThat(timeline.size(), is(2));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(2));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("query_type", "dhcid", 2),
                AggregationUtils.createBucketElement("query_type", "dlv", 2)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));

        JsonArray lastHourBuckets = AggregationUtils.getTimeAggregation(timestamp.minusHours(1).minusMinutes(1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(lastHourBuckets.size(), is(2));
        JsonObject[] expectedLastHourBuckets = {
                AggregationUtils.createBucketElement("query_type", "dname", 2),
                AggregationUtils.createBucketElement("query_type", "dnskey", 2)
        };
        assertThat(lastHourBuckets, containsInAnyOrder(expectedLastHourBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryType_withQueryTypeParamTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/older", timestamp.minusHours(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/too_old", timestamp.minusDays(1).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "aggregate=query_type&query_type=dhcid");

        assertThat(timeline.size(), is(1));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(1));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("query_type", "dhcid", 2)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryType_withDomainParamTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/older", timestamp.minusHours(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/too_old", timestamp.minusDays(1).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "aggregate=query_type&domain=whalebone.io");

        assertThat(timeline.size(), is(2));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(1));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("query_type", "dlv", 1)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));

        JsonArray lastHourBuckets = AggregationUtils.getTimeAggregation(timestamp.minusHours(1).minusMinutes(1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(lastHourBuckets.size(), is(1));
        JsonObject[] expectedLastHourBuckets = {
                AggregationUtils.createBucketElement("query_type", "dnskey", 1)
        };
        assertThat(lastHourBuckets, containsInAnyOrder(expectedLastHourBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryType_withQueryParamTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/older", timestamp.minusHours(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/too_old", timestamp.minusDays(1).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "aggregate=query_type&query=wtf.whalebone.io");

        assertThat(timeline.size(), is(2));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(1));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("query_type", "dhcid", 1)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));

        JsonArray lastHourBuckets = AggregationUtils.getTimeAggregation(timestamp.minusHours(1).minusMinutes(1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(lastHourBuckets.size(), is(1));
        JsonObject[] expectedLastHourBuckets = {
                AggregationUtils.createBucketElement("query_type", "dname", 1)
        };
        assertThat(lastHourBuckets, containsInAnyOrder(expectedLastHourBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryType_twoDaysDaysTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/older", timestamp.minusDays(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_query_type/too_old", timestamp.minusDays(2).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "aggregate=query_type&days=2");

        assertThat(timeline.size(), is(2));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(2));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("query_type", "dhcid", 2),
                AggregationUtils.createBucketElement("query_type", "dlv", 2)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));

        JsonArray lastHourBuckets = AggregationUtils.getTimeAggregation(timestamp.minusDays(1).minusMinutes(1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(lastHourBuckets.size(), is(2));
        JsonObject[] expectedLastHourBuckets = {
                AggregationUtils.createBucketElement("query_type", "dname", 2),
                AggregationUtils.createBucketElement("query_type", "dnskey", 2)
        };
        assertThat(lastHourBuckets, containsInAnyOrder(expectedLastHourBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByTldTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_tld/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_tld/older", timestamp.minusHours(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_tld/too_old", timestamp.minusDays(1).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "aggregate=tld");

        assertThat(timeline.size(), is(2));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(2));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("tld", "com", 2),
                AggregationUtils.createBucketElement("tld", "org", 2)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));

        JsonArray lastHourBuckets = AggregationUtils.getTimeAggregation(timestamp.minusHours(1).minusMinutes(1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(lastHourBuckets.size(), is(2));
        JsonObject[] expectedLastHourBuckets = {
                AggregationUtils.createBucketElement("tld", "cz", 2),
                AggregationUtils.createBucketElement("tld", "sk", 2)
        };
        assertThat(lastHourBuckets, containsInAnyOrder(expectedLastHourBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByTld_withResolverIdParamTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_tld/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_tld/older", timestamp.minusHours(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_tld/too_old", timestamp.minusDays(1).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "aggregate=tld&resolver_id=3");

        assertThat(timeline.size(), is(2));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(1));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("tld", "com", 1)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));

        JsonArray lastHourBuckets = AggregationUtils.getTimeAggregation(timestamp.minusHours(1).minusMinutes(1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(lastHourBuckets.size(), is(1));
        JsonObject[] expectedLastHourBuckets = {
                AggregationUtils.createBucketElement("tld", "cz", 1)
        };
        assertThat(lastHourBuckets, containsInAnyOrder(expectedLastHourBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByDomainTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_domain/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_domain/older", timestamp.minusHours(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_domain/too_old", timestamp.minusDays(1).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "aggregate=domain");

        assertThat(timeline.size(), is(2));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(2));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("domain", "google.com", 2),
                AggregationUtils.createBucketElement("domain", "whalebone.io", 2)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));

        JsonArray lastHourBuckets = AggregationUtils.getTimeAggregation(timestamp.minusHours(1).minusMinutes(1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(lastHourBuckets.size(), is(2));
        JsonObject[] expectedLastHourBuckets = {
                AggregationUtils.createBucketElement("domain", "google.com", 2),
                AggregationUtils.createBucketElement("domain", "whalebone.io", 2)
        };
        assertThat(lastHourBuckets, containsInAnyOrder(expectedLastHourBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByDomain_withTldParameterTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_domain/now", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_domain/older", timestamp.minusHours(1).minusMinutes(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/by_domain/too_old", timestamp.minusDays(1).minusMinutes(1));
        JsonArray timeline = getTimeline(context, "aggregate=domain&tld=io");

        assertThat(timeline.size(), is(2));
        JsonArray thisHourBuckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(thisHourBuckets.size(), is(1));
        JsonObject[] expectedThisOurBuckets = {
                AggregationUtils.createBucketElement("domain", "whalebone.io", 2)
        };
        assertThat(thisHourBuckets, containsInAnyOrder(expectedThisOurBuckets));

        JsonArray lastHourBuckets = AggregationUtils.getTimeAggregation(timestamp.minusHours(1).minusMinutes(1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(lastHourBuckets.size(), is(1));
        JsonObject[] expectedLastHourBuckets = {
                AggregationUtils.createBucketElement("domain", "whalebone.io", 2)
        };
        assertThat(lastHourBuckets, containsInAnyOrder(expectedLastHourBuckets));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryTypeDayBucketIntervalTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/interval/bucket0", timestamp);
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/interval/bucket1", timestamp.minusDays(1));
        archiveInitiator.sendMultipleDnsSecLogs("dnssec/interval/bucket2", timestamp.minusDays(2));
        JsonArray timeline = getTimeline(context, "days=2&interval=day");

        assertThat(timeline.size(), is(2));

        JsonArray buckets0 = AggregationUtils.getTimeAggregation(timestamp, timeline, ChronoUnit.DAYS).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBuckets0 = {
                AggregationUtils.createBucketElement("query_type", "dhcid", 2),
                AggregationUtils.createBucketElement("query_type", "dlv", 2)

        };
        assertThat(buckets0, Matchers.containsInAnyOrder(expectedBuckets0));

        JsonArray buckets1 = AggregationUtils.getTimeAggregation(timestamp.minusDays(1), timeline, ChronoUnit.DAYS).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBuckets1 = {
                AggregationUtils.createBucketElement("query_type", "cdnskey", 3),
                AggregationUtils.createBucketElement("query_type", "cert", 2),
                AggregationUtils.createBucketElement("query_type", "dlv", 1)
        };
        assertThat(buckets1, Matchers.containsInAnyOrder(expectedBuckets1));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidResolverIdTest(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = invalidRequest(context, "resolver_id=abc");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("resolver_id", "abc", 21, "INVALID_PARAM_VALUE",
                "Invalid value - value must be an integer", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidDaysTest(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = invalidRequest(context, "days=noInt");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "noInt", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 14>", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidMinDaysTest(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = invalidRequest(context, "days=0");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "0", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 14>", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidMaxDaysTest(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = invalidRequest(context, "days=15");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "15", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 14>", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidQueryTypeTest(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = invalidRequest(context, "query_type=invalid");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("query_type", "invalid", 21, "INVALID_PARAM_VALUE", "Invalid enum value",
                Arrays.stream(EDnsQueryType.values()).map(qt -> qt.name().toLowerCase()).toArray(String[]::new))));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidAggregateTest(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = invalidRequest(context, "aggregate=invalid");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("aggregate", "invalid", 21, "INVALID_PARAM_VALUE", "Invalid enum value",
                Arrays.stream(EDnsSecAggregate.values()).map(qt -> qt.name().toLowerCase()).toArray(String[]::new))));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidIntervalParamTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = invalidRequest(context, "interval=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("interval", "xyz", 21, "INVALID_PARAM_VALUE", "Invalid enum value",
                new String[]{"hour", "day", "week"})));
    }

    private static JsonArray getTimeline(URL context, String queryString) throws IOException {
        return AggregationUtils.getAggregationBucketsArray(context, "1/dnssec/timeline?" + queryString, TOKEN);
    }

    private static JsonArray invalidRequest(URL context, String queryString) throws IOException {
        return InvalidRequestUtils.sendInvalidRequest(context, "1/dnssec/timeline?" + queryString, TOKEN);
    }

    /**
     * returns timestamp that would be used in testing data
     * 1 min minus to fit in search time range filter
     */
    private ZonedDateTime timestamp() {
        return Instant.now().atZone(ZoneId.of("UTC")).minusMinutes(1);
    }
}
