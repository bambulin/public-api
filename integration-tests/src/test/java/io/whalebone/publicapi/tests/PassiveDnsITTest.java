package io.whalebone.publicapi.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.whalebone.publicapi.ejb.dto.aggregate.EDnsAggregate;
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
import static org.hamcrest.core.Is.is;

public class PassiveDnsITTest extends Arquillian {
    private static final String CLIENT_ID = "2";

    private ArchiveInitiator archiveInitiator;

    public PassiveDnsITTest() {
        archiveInitiator = new ArchiveInitiator();
    }

    @BeforeMethod
    public void setUp() throws IOException {
        archiveInitiator.cleanDnsLogs();
    }

    /**
     * Tests aggregate by query when there is only one time bucket
     * Tests the working of days param aswell
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryType2DaysTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query_type", timestamp);
        archiveInitiator.sendDnsLog("passivedns/by_query_type/older/passivedns-query_type-a_older.json", timestamp.minusMinutes(2 * 24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "days=2");

        checkCounts(timeline);
        assertThat(timeline.size(), is(1));
        JsonArray buckets = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();

        JsonObject[] expectedBuckets = {
                AggregationUtils.createBucketElement("query_type", "a", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 1)
        };

        assertThat(buckets, Matchers.containsInAnyOrder(expectedBuckets));
    }

    /**
     * Tests aggregate by query when there is more than one time bucket
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryType3DaysTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query_type", timestamp);
        archiveInitiator.sendDnsLog("passivedns/by_query_type/older/passivedns-query_type-a_older.json", timestamp.minusMinutes(2 * 24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "days=3");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(2 * 24 * 60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query_type", "a", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("query_type", "a", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregate by query when there is more than one time bucket
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryTypeDayBucketIntervalTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/days_interval/day0", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/days_interval/day1", timestamp.minusDays(1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/days_interval/day2", timestamp.minusDays(2));
        JsonArray timeline = getTimeline(context, "days=5&interval=day");

        checkCounts(timeline);
        assertThat(timeline.size(), is(3));

        JsonArray bucketsDay0 = AggregationUtils.getTimeAggregation(timestamp, timeline, ChronoUnit.DAYS).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsDay0 = {
                AggregationUtils.createBucketElement("query_type", "a", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 1)

        };
        assertThat(bucketsDay0, Matchers.containsInAnyOrder(expectedBucketsDay0));

        JsonArray bucketsDay1 = AggregationUtils.getTimeAggregation(timestamp.minusDays(1), timeline, ChronoUnit.DAYS).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsDay1 = {
                AggregationUtils.createBucketElement("query_type", "aaaa", 2),
                AggregationUtils.createBucketElement("query_type", "tkey", 2)
        };
        assertThat(bucketsDay1, Matchers.containsInAnyOrder(expectedBucketsDay1));

        JsonArray bucketsDay2 = AggregationUtils.getTimeAggregation(timestamp.minusDays(2), timeline, ChronoUnit.DAYS).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsDay2 = {
                AggregationUtils.createBucketElement("query_type", "afsdb", 2),
                AggregationUtils.createBucketElement("query_type", "hip", 3)
        };
        assertThat(bucketsDay2, Matchers.containsInAnyOrder(expectedBucketsDay2));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryTypeWithQueryParamTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/query_param/now", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/query_param/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/query_param/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=query_type&query=abc.whalebone.io");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(bucketsOld.size(), is(1));
        assertThat(bucketsOld.get(0), is(AggregationUtils.createBucketElement("query_type", "a", 1)));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        assertThat(bucketsNew.size(), is(1));
        assertThat(bucketsNew.get(0), is(AggregationUtils.createBucketElement("query_type", "a", 1)));
    }

    /**
     * Tests aggregate by client_ip
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByClientIpTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=client_ip");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();

        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("client_ip", "1.2.3.4", 3),
                AggregationUtils.createBucketElement("client_ip", "3.3.3.3", 1),
                AggregationUtils.createBucketElement("client_ip", "1.2.3.5", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("client_ip", "1.2.3.4", 3),
                AggregationUtils.createBucketElement("client_ip", "2.2.2.2", 1),
                AggregationUtils.createBucketElement("client_ip", "1.2.3.5", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by client_ip
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByClientIpTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "client_ip=1.2.3.4");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query_type", "apl", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("query_type", "a", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by client_ip using wildcards
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByClientIpWildcardTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "client_ip=1.2.3.*");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query_type", "apl", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 2)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("query_type", "a", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 1),
                AggregationUtils.createBucketElement("query_type", "ta", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregate by answer
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByAnswerTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=answer");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();

        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("answer", "1.1.1.2", 3),
                AggregationUtils.createBucketElement("answer", "3.1.1.1", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("answer", "1.1.1.2", 3),
                AggregationUtils.createBucketElement("answer", "1.1.1.3", 1),
                AggregationUtils.createBucketElement("answer", "3.1.1.1", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by answer
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByAnswerWildcardTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "answer=1.1.1.*");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query_type", "ta", 2),
                AggregationUtils.createBucketElement("query_type", "a", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("query_type", "a", 2),
                AggregationUtils.createBucketElement("query_type", "ta", 2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregation by tld
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByTldTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_tld", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_tld/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_tld/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=tld");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("tld", "eu", 3),
                AggregationUtils.createBucketElement("tld", "com", 2)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("tld", "eu", 3),
                AggregationUtils.createBucketElement("tld", "com", 3)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregation by domain
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByDomainTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=domain");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("domain", "domain2.eu", 3),
                AggregationUtils.createBucketElement("domain", "some-other-domain.com", 1),
                AggregationUtils.createBucketElement("domain", "some-domain.com", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("domain", "domain2.eu", 2),
                AggregationUtils.createBucketElement("domain", "domain.eu", 1),
                AggregationUtils.createBucketElement("domain", "some-other-domain.com", 1),
                AggregationUtils.createBucketElement("domain", "some-domain.com", 2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filtering by domain combined with aggregation by query
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByDomainAggregateByQueryTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=query&domain=*domain.com");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query", "some-other-domain.com", 1),
                AggregationUtils.createBucketElement("query", "subdomain2.some-domain.com", 1),
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("query", "some-other-domain.com", 1),
                AggregationUtils.createBucketElement("query", "subdomain.some-domain.com", 1),
                AggregationUtils.createBucketElement("query", "subdomain2.some-domain.com", 1),
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregation by query
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=query");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query", "abc.domain2.eu", 1),
                AggregationUtils.createBucketElement("query", "shop.domain2.eu", 2),
                AggregationUtils.createBucketElement("query", "some-other-domain.com", 1),
                AggregationUtils.createBucketElement("query", "subdomain2.some-domain.com", 1),
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("query", "abc.domain2.eu", 1),
                AggregationUtils.createBucketElement("query", "shop.domain2.eu", 1),
                AggregationUtils.createBucketElement("query", "domain.eu", 1),
                AggregationUtils.createBucketElement("query", "subdomain.some-domain.com", 1),
                AggregationUtils.createBucketElement("query", "subdomain2.some-domain.com", 1),
                AggregationUtils.createBucketElement("query", "some-other-domain.com", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filtering by dga
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByDgaTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_dga", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_dga/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_dga/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=domain&dga=true");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("domain", "some-domain.com", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("domain", "domain.eu", 1),
                AggregationUtils.createBucketElement("domain", "some-domain.com", 2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by resolver_id
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByResolverIdTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_resolver_id", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_resolver_id/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_resolver_id/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "resolver_id=10");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query_type", "apl", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("query_type", "a", 2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by device_id
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByDeviceIdTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_device_id", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_device_id/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_device_id/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "device_id=device1");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query_type", "apl", 2),
                AggregationUtils.createBucketElement("query_type", "rp", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("query_type", "a", 2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregation by device_id
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByDeviceIdTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_device_id", timestamp);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_device_id/older", timestamp.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_device_id/outdated", timestamp.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=device_id");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("device_id", "device1", 3),
                AggregationUtils.createBucketElement("device_id", "device4", 1),
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                AggregationUtils.createBucketElement("device_id", "device1", 2),
                AggregationUtils.createBucketElement("device_id", "device2", 1),
                AggregationUtils.createBucketElement("device_id", "device3", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests the validation of query_type param
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void queryTypeValidationTest(@ArquillianResource URL context) throws Exception {
        String[] queryTypes = {"a", "aaaa", "afsdb", "apl", "caa", "cdnskey", "cds", "cert", "cname",
                "dhcid", "dlv", "dname", "dnskey", "ds", "hip", "ipseckey", "key", "kx", "loc",
                "mx", "naptr", "ns", "nsec", "nsec3", "nsec3param", "openpgpkey", "ptr", "rrsig",
                "rp", "sig", "soa", "srv", "sshfp", "ta", "tkey", "tlsa", "tsig", "txt", "uri", "aname"};
        JsonArray jsonErrors = getTimelineInvalid(context, "query_type=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("query_type", "xyz", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid enum value", queryTypes)));
    }

    /**
     * Tests the validation of aggregate param
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateValidationTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getTimelineInvalid(context, "aggregate=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("aggregate", "xyz", 21, "INVALID_PARAM_VALUE", "" +
                        "Invalid enum value",
                new String[]{"client_ip", "tld", "domain", "query", "query_type", "answer", "device_id"})));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidIntervalParamTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getTimelineInvalid(context, "interval=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("interval", "xyz", 21, "INVALID_PARAM_VALUE", "Invalid enum value",
                new String[]{"hour", "day", "week"})));
    }

    /**
     * Tests the validation of days for days greater than MAX_DAYS
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void notIntegerDaysTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getTimelineInvalid(context, "days=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "xyz", 21, "INVALID_PARAM_VALUE",
                "Invalid value - value must be an integer in range <1 - 14>", null)));
    }

    /**
     * Tests the validation of days for the case when days=-1
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void minDaysTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getTimelineInvalid(context, "days=0");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "0", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 14>", null)));
    }

    /**
     * Tests the validation of days for the case when a non-integer is passed
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void maxDaysTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getTimelineInvalid(context, "days=15");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "15", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 14>", null)));
    }

    /**
     * Tests the validation of resolver_id param
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void resolverIdValidationTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getTimelineInvalid(context, "resolver_id=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("resolver_id", "xyz", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer", null)));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidAggregateTest(@ArquillianResource URL context) throws IOException {
        JsonArray jsonErrors = getTimelineInvalid(context, "aggregate=invalid");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("aggregate", "invalid", 21, "INVALID_PARAM_VALUE", "Invalid enum value",
                Arrays.stream(EDnsAggregate.values()).map(qt -> qt.name().toLowerCase()).toArray(String[]::new))));
    }

    /**
     * Tests the behaviour with an invalid parameter
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidParameterTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendDnsLog("passivedns/by_query_type/passivedns-query_type-a.json", timestamp);

        JsonArray timeline = getTimeline(context, "not_a_parameter=abc");
        assertThat(timeline.size(), is(1));

        JsonArray bucketsOld = AggregationUtils.getTimeAggregation(timestamp, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                AggregationUtils.createBucketElement("query_type", "a", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));
    }

    private static JsonArray getTimeline(URL context, String queryString) throws IOException {
        return AggregationUtils.getAggregationBucketsArray(context, "1/dns/timeline?" + queryString, CLIENT_ID);
    }

    /**
     * Checks whether count field for each time aggregation is the sum of the count fields of the buckets
     *
     * @param timeline
     */
    private static void checkCounts(JsonArray timeline) {
        for (int i = 0; i < timeline.size(); i++) {
            int count = timeline.get(i).getAsJsonObject().get("count").getAsInt();
            JsonArray buckets = timeline.get(i).getAsJsonObject().get("buckets").getAsJsonArray();
            int sum = 0;
            for (int j = 0; j < buckets.size(); j++) {
                sum = sum + buckets.get(j).getAsJsonObject().get("count").getAsInt();
            }
            assertThat(count, is(sum));
        }
    }

    private static JsonArray getTimelineInvalid(URL context, String queryString) throws IOException {
        return InvalidRequestUtils.sendInvalidRequest(context, "1/dns/timeline?" + queryString, CLIENT_ID);
    }

    /**
     * returns timestamp that would be used in testing data
     * 1 min minus to fit in search time range filter
     */
    private ZonedDateTime timestamp() {
        return Instant.now().atZone(ZoneId.of("UTC")).minusMinutes(1);
    }
}
