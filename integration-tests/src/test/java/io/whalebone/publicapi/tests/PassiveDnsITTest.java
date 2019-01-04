package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.whalebone.publicapi.tests.matchers.ParamValidationErrorMatcher;
import org.hamcrest.Matchers;
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
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class PassiveDnsITTest extends Arquillian {

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
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryType2DaysTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query_type", now);
        archiveInitiator.sendDnsLog("passivedns/by_query_type/older/passivedns-query_type-a_older.json", now.minusMinutes(2 * 24 * 60 +1));
        JsonArray timeline = getTimeline(context, "days=2");

        checkCounts(timeline);
        assertThat(timeline.size(), is(1));
        JsonArray buckets = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();

        JsonObject[] expectedBuckets = {
                createBucketElement("query_type", "a", 2),
                createBucketElement("query_type", "rp", 1)
        };

        assertThat(buckets, Matchers.containsInAnyOrder(expectedBuckets));
    }

    /**
     * Tests aggregate by query when there is more than one time bucket
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryType3DaysTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query_type", now);
        archiveInitiator.sendDnsLog("passivedns/by_query_type/older/passivedns-query_type-a_older.json", now.minusMinutes(2 * 24 * 60 +1));
        JsonArray timeline = getTimeline(context, "days=3");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(2 * 24 * 60 +1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("query_type","a",1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("query_type", "a", 2),
                createBucketElement("query_type", "rp", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregate by client_ip
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByClientIpTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/older", now.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/outdated", now.minusMinutes(24 * 60  + 1));
        JsonArray timeline = getTimeline(context,"aggregate=client_ip");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();

        JsonObject[] expectedBucketsOld  = {
                createBucketElement("client_ip", "1.2.3.4", 3),
                createBucketElement("client_ip", "3.3.3.3", 1),
                createBucketElement("client_ip", "1.2.3.5", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("client_ip", "1.2.3.4", 3),
                createBucketElement("client_ip", "2.2.2.2", 1),
                createBucketElement("client_ip", "1.2.3.5", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by client_ip
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByClientIpTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/older", now.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/outdated", now.minusMinutes(24 * 60  + 1));
        JsonArray timeline = getTimeline(context, "client_ip=1.2.3.4");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
            createBucketElement("query_type", "apl", 2),
            createBucketElement("query_type", "rp", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
            createBucketElement("query_type", "a", 2),
            createBucketElement("query_type", "rp", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by client_ip using wildcards
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByClientIpWildcardTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/older", now.minusMinutes(60 +1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_client_ip/outdated", now.minusMinutes(24 * 60 +1));
        JsonArray timeline = getTimeline(context, "client_ip=1.2.3.*");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("query_type","apl",2),
                createBucketElement("query_type","rp",2)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("query_type","a",2),
                createBucketElement("query_type","rp",1),
                createBucketElement("query_type","ta",1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregate by answer
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByAnswerTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer/older", now.minusMinutes(60 +1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer/outdated", now.minusMinutes(24 * 60 +1));
        JsonArray timeline = getTimeline(context, "aggregate=answer");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();

        JsonObject[] expectedBucketsOld  = {
                createBucketElement("answer",   "1.1.1.2", 3),
                createBucketElement("answer", "3.1.1.1", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("answer", "1.1.1.2", 3),
                createBucketElement("answer", "1.1.1.3", 1),
                createBucketElement("answer", "3.1.1.1", 1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by answer
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByAnswerWildcardTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer/older", now.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_answer/outdated", now.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "answer=1.1.1.*");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("query_type", "ta", 2),
                createBucketElement("query_type", "a", 1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("query_type", "a", 2),
                createBucketElement("query_type", "ta", 2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }
    /**
     * Tests aggregation by tld
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByTldTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_tld", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_tld/older", now.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_tld/outdated", now.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=tld");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("tld","eu",3),
                createBucketElement("tld","com",2)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("tld","eu",3),
                createBucketElement("tld","com",3)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregation by domain
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByDomainTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain/older", now.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain/outdated", now.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=domain");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("domain","domain2.eu",3),
                createBucketElement("domain","some-other-domain.com",1),
                createBucketElement("domain","some-domain.com",1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("domain","domain2.eu",2),
                createBucketElement("domain","domain.eu",1),
                createBucketElement("domain","some-other-domain.com",1),
                createBucketElement("domain","some-domain.com",2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filtering by domain combined with aggregation by query
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByDomainAggregateByQueryTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain/older", now.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_domain/outdated", now.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=query&domain=*domain.com");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("query","some-other-domain.com",1),
                createBucketElement("query","subdomain2.some-domain.com",1),
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("query","some-other-domain.com",1),
                createBucketElement("query","subdomain.some-domain.com",1),
                createBucketElement("query","subdomain2.some-domain.com",1),
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests aggregation by query
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void aggregateByQueryTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query/older", now.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query/outdated", now.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=query");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("query","abc.domain2.eu",1),
                createBucketElement("query","shop.domain2.eu",2),
                createBucketElement("query","some-other-domain.com",1),
                createBucketElement("query","subdomain2.some-domain.com",1),
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("query","abc.domain2.eu",1),
                createBucketElement("query","shop.domain2.eu",1),
                createBucketElement("query","domain.eu",1),
                createBucketElement("query","subdomain.some-domain.com",1),
                createBucketElement("query","subdomain2.some-domain.com",1),
                createBucketElement("query","some-other-domain.com",1)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filtering by dga
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByDgaTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_dga", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_dga/older", now.minusMinutes(60 + 1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_dga/outdated", now.minusMinutes(24 * 60 + 1));
        JsonArray timeline = getTimeline(context, "aggregate=domain&dga=true");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("domain","some-domain.com",1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("domain","domain.eu",1),
                createBucketElement("domain","some-domain.com",2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests filter by resolver_id
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void filterByResolverIdTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_resolver_id", now);
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_resolver_id/older", now.minusMinutes(60 +1));
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_resolver_id/outdated", now.minusMinutes(24 * 60 +1));
        JsonArray timeline = getTimeline(context, "resolver_id=10");

        checkCounts(timeline);
        assertThat(timeline.size(), is(2));

        JsonArray bucketsOld = getAggregations(now.minusMinutes(60 + 1), timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("query_type","apl",2),
                createBucketElement("query_type","rp",1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));

        JsonArray bucketsNew = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsNew = {
                createBucketElement("query_type","a",2)
        };

        assertThat(bucketsNew, Matchers.containsInAnyOrder(expectedBucketsNew));
    }

    /**
     * Tests the validation of query_type param
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void queryTypeValidationTest(@ArquillianResource URL context) throws Exception {
        String [] queryTypes = {"a","aaaa","afsdb","apl","caa","cdnskey","cds","cert","cname",
                "dhcid","dlv","dname","dnskey","ds","hip","ipseckey","key","kx","loc",
                "mx","naptr","ns","nsec","nsec3","nsec3param","openpgpkey","ptr","rrsig",
                "rp","sig","soa","srv","sshfp","ta","tkey","tlsa","tsig","txt","uri","aname"};
        JsonArray jsonErrors = getTimelineInvalid(context, "query_type=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("query_type", "xyz", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid enum value", queryTypes)));
    }

    /**
     * Tests the validation of aggregate param
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
                new String[] {"client_ip","tld", "domain", "query", "query_type", "answer"})));
    }

    /**
     * Tests the validation of days for days greater than MAX_DAYS
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void notIntegerDaysTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getTimelineInvalid(context, "days=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "xyz", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 14>", null)));
    }

    /**
     * Tests the validation of days for the case when days=-1
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

    /**
     * Tests the behaviour with an invalid parameter
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidParameterTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        archiveInitiator.sendDnsLog("passivedns/by_query_type/passivedns-query_type-a.json", now);

        JsonArray timeline = getTimeline(context,"not_a_parameter=abc");
        assertThat(timeline.size(), is(1));

        JsonArray bucketsOld = getAggregations(now, timeline).getAsJsonObject().get("buckets").getAsJsonArray();
        JsonObject[] expectedBucketsOld = {
                createBucketElement("query_type","a",1)
        };

        assertThat(bucketsOld, Matchers.containsInAnyOrder(expectedBucketsOld));
    }

    JsonObject createBucketElement (String aggregate, String value, Integer count){
        JsonObject element = new JsonObject();
        element.addProperty(aggregate,value);
        element.addProperty("count", count);
        return element;
    }

    private static JsonArray getTimeline(URL context, String queryString) throws IOException {
        WebClient webClient = new WebClient();
        WebRequest requestSettings = new WebRequest(new URL(context + "1/dns/timeline?" + queryString), HttpMethod.GET);
        requestSettings.setAdditionalHeader("whalebone_client_id", "2");
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertThat(page.getWebResponse().getStatusCode(), is(HttpURLConnection.HTTP_OK));
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(page.getWebResponse().getContentAsString());
        return element.getAsJsonArray();
    }

    /**
     * Returns aggregations corresponding to time from timeline
     * If there is none, an exception is thrown
     * @param time
     * @param timeline
     * @return
     */
    private static JsonObject getAggregations(ZonedDateTime time, JsonArray timeline) throws Exception{
        final String PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);

        String timestamp = time.truncatedTo(ChronoUnit.HOURS).format(formatter);
        for (int i = 0; i < timeline.size(); i++) {
            if (timeline.get(i).getAsJsonObject().get("timestamp").getAsString().equals(timestamp)){
               return timeline.get(i).getAsJsonObject();
            }
        }
        throw new Exception("Expected timeline :" + timeline.toString() +
                " to contain timestamp: " + timestamp + " but it didn't.");
    }

    /**
     * Checks whether count field for each time aggregation is the sum of the count fields of the buckets
     * @param timeline
     */
    private static void checkCounts(JsonArray timeline) {
        for (int i = 0; i < timeline.size(); i++) {
            int count = timeline.get(i).getAsJsonObject().get("count").getAsInt();
            JsonArray buckets = timeline.get(i).getAsJsonObject().get("buckets").getAsJsonArray();
            int sum = 0;
            for(int j =0; j < buckets.size(); j++) {
                sum = sum + buckets.get(j).getAsJsonObject().get("count").getAsInt();
            }
            assertThat(count, is(sum));
        }
    }

    private static JsonArray getTimelineInvalid(URL context, String queryString) throws IOException {
        WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        WebRequest requestSettings = new WebRequest(new URL(context + "1/dns/timeline?" + queryString), HttpMethod.GET);
        requestSettings.setAdditionalHeader("whalebone_client_id", "2");
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

    private static ParamValidationErrorMatcher error(String parameter, String value, int errorCode, String errorType,
                                                     String message, String[] acceptedValues) {
        return new ParamValidationErrorMatcher(parameter, value, errorCode, errorType, message, acceptedValues);
    }

}
