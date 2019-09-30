package io.whalebone.publicapi.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.whalebone.publicapi.tests.utils.AggregationUtils;
import io.whalebone.publicapi.tests.utils.InvalidRequestUtils;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static io.whalebone.publicapi.tests.matchers.ParamValidationErrorMatcher.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

public class ResolverMetricsITTest extends Arquillian {
    private static final String CLIENT_ID = "2";

    private ArchiveInitiator archiveInitiator;

    public ResolverMetricsITTest() {
        archiveInitiator = new ArchiveInitiator();
    }

    @BeforeMethod
    public void setUp() throws IOException {
        archiveInitiator.cleanResolverSysInfo();
    }

    /**
     * Tests gets metrics retrieved by default criteria
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void defaultIntervalDefaultDaysNoResolverIdParamTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleResolverMetrics("resolver/new", timestamp);
        archiveInitiator.sendMultipleResolverMetrics("resolver/old", timestamp.truncatedTo(ChronoUnit.HOURS));
        archiveInitiator.sendMultipleResolverMetrics("resolver/older", timestamp.minusHours(1));
        archiveInitiator.sendMultipleResolverMetrics("resolver/oldest", timestamp.minusHours(1).truncatedTo(ChronoUnit.HOURS));
        archiveInitiator.sendMultipleResolverMetrics("resolver/outdated", timestamp.minusDays(1));
        JsonArray metrics = getMetrics(context, "");
        assertThat(metrics.size(), is(2));

        String newestTimeBucketTimestamp = formatTimestamp(timestamp.truncatedTo(ChronoUnit.HOURS));
        String olderTimeBucketTimestamp = formatTimestamp(timestamp.minusHours(1).truncatedTo(ChronoUnit.HOURS));

        assertThat(metrics, containsInAnyOrder(
                resolverMetrics(42, "hostname1", timeline(
                        timelineBucket(olderTimeBucketTimestamp, usage(0.75, 53.95, 14.05, 0.25), latency(15, 11, 21, 9, 17, 19, 7, 13), "fail"),
                        timelineBucket(newestTimeBucketTimestamp, usage(0.51, 51.75, 13.85, 0.05), latency(11, 7, 17, 5, 13, 15, 3, 9), "ok")
                )),
                resolverMetrics(43, "hostnameLatest", timeline(
                        timelineBucket(olderTimeBucketTimestamp, usage(0.28, 45.35, 12.75, 1.25), latency(45, 27, 72, 18, 54, 63, 9, 36), "fail"),
                        timelineBucket(newestTimeBucketTimestamp, usage(0.26, 45.15, 12.55, 1.1), latency(25, 15, 40, 10, 30, 35, 5, 20), "ok")
                ))
        ));
    }

    /**
     * Tests gets metrics of resolver 42
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void resolverIdParamTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleResolverMetrics("resolver/new", timestamp);
        archiveInitiator.sendMultipleResolverMetrics("resolver/old", timestamp.truncatedTo(ChronoUnit.HOURS));
        archiveInitiator.sendMultipleResolverMetrics("resolver/older", timestamp.minusHours(1));
        archiveInitiator.sendMultipleResolverMetrics("resolver/oldest", timestamp.minusHours(1).truncatedTo(ChronoUnit.HOURS));
        archiveInitiator.sendMultipleResolverMetrics("resolver/outdated", timestamp.minusDays(1));
        JsonArray metrics = getMetrics(context, "resolver_id=42");
        assertThat(metrics.size(), is(1));

        String newestTimeBucketTimestamp = formatTimestamp(timestamp.truncatedTo(ChronoUnit.HOURS));
        String olderTimeBucketTimestamp = formatTimestamp(timestamp.minusHours(1).truncatedTo(ChronoUnit.HOURS));

        assertThat(metrics, containsInAnyOrder(
                resolverMetrics(42, "hostname1", timeline(
                        timelineBucket(olderTimeBucketTimestamp, usage(0.75, 53.95, 14.05, 0.25), latency(15, 11, 21, 9, 17, 19, 7, 13), "fail"),
                        timelineBucket(newestTimeBucketTimestamp, usage(0.51, 51.75, 13.85, 0.05), latency(11, 7, 17, 5, 13, 15, 3, 9), "ok")
                ))
        ));
    }

    /**
     * Tests gets metrics for last two days
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void daysParamTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleResolverMetrics("resolver/new", timestamp);
        archiveInitiator.sendMultipleResolverMetrics("resolver/old", timestamp.truncatedTo(ChronoUnit.HOURS));
        archiveInitiator.sendMultipleResolverMetrics("resolver/older", timestamp.minusDays(1));
        archiveInitiator.sendMultipleResolverMetrics("resolver/oldest", timestamp.minusDays(1).truncatedTo(ChronoUnit.HOURS));
        archiveInitiator.sendMultipleResolverMetrics("resolver/outdated", timestamp.minusDays(2));
        JsonArray metrics = getMetrics(context, "days=2");
        assertThat(metrics.size(), is(2));

        String newestTimeBucketTimestamp = formatTimestamp(timestamp.truncatedTo(ChronoUnit.HOURS));
        String olderTimeBucketTimestamp = formatTimestamp(timestamp.minusDays(1).truncatedTo(ChronoUnit.HOURS));

        assertThat(metrics, containsInAnyOrder(
                resolverMetrics(42, "hostname1", timeline(
                        timelineBucket(olderTimeBucketTimestamp, usage(0.75, 53.95, 14.05, 0.25), latency(15, 11, 21, 9, 17, 19, 7, 13), "fail"),
                        timelineBucket(newestTimeBucketTimestamp, usage(0.51, 51.75, 13.85, 0.05), latency(11, 7, 17, 5, 13, 15, 3, 9), "ok")
                )),
                resolverMetrics(43, "hostnameLatest", timeline(
                        timelineBucket(olderTimeBucketTimestamp, usage(0.28, 45.35, 12.75, 1.25), latency(45, 27, 72, 18, 54, 63, 9, 36), "fail"),
                        timelineBucket(newestTimeBucketTimestamp, usage(0.26, 45.15, 12.55, 1.1), latency(25, 15, 40, 10, 30, 35, 5, 20), "ok")
                ))
        ));
    }

    /**
     * Tests gets metrics for last two days
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void intervalTest(@ArquillianResource URL context) throws Exception {
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleResolverMetrics("resolver/new", timestamp);
        archiveInitiator.sendMultipleResolverMetrics("resolver/old", timestamp.truncatedTo(ChronoUnit.DAYS));
        archiveInitiator.sendMultipleResolverMetrics("resolver/older", timestamp.minusDays(1));
        archiveInitiator.sendMultipleResolverMetrics("resolver/oldest", timestamp.minusDays(1).truncatedTo(ChronoUnit.DAYS));
        archiveInitiator.sendMultipleResolverMetrics("resolver/outdated", timestamp.minusDays(2));
        JsonArray metrics = getMetrics(context, "interval=day&days=2");
        assertThat(metrics.size(), is(2));

        String newestTimeBucketTimestamp = formatTimestamp(timestamp.truncatedTo(ChronoUnit.DAYS));
        String olderTimeBucketTimestamp = formatTimestamp(timestamp.minusDays(1).truncatedTo(ChronoUnit.DAYS));

        assertThat(metrics, containsInAnyOrder(
                resolverMetrics(42, "hostname1", timeline(
                        timelineBucket(olderTimeBucketTimestamp, usage(0.75, 53.95, 14.05, 0.25), latency(15, 11, 21, 9, 17, 19, 7, 13), "fail"),
                        timelineBucket(newestTimeBucketTimestamp, usage(0.51, 51.75, 13.85, 0.05), latency(11, 7, 17, 5, 13, 15, 3, 9), "ok")
                )),
                resolverMetrics(43, "hostnameLatest", timeline(
                        timelineBucket(olderTimeBucketTimestamp, usage(0.28, 45.35, 12.75, 1.25), latency(45, 27, 72, 18, 54, 63, 9, 36), "fail"),
                        timelineBucket(newestTimeBucketTimestamp, usage(0.26, 45.15, 12.55, 1.1), latency(25, 15, 40, 10, 30, 35, 5, 20), "ok")
                ))
        ));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void invalidIntervalParamTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getMetricsInvalid(context, "interval=xyz");
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
        JsonArray jsonErrors = getMetricsInvalid(context, "days=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "xyz", 21, "INVALID_PARAM_VALUE",
                "Invalid value - value must be an integer in range <1 - 30>", null)));
    }

    /**
     * Tests the validation of days for the case when days=0
     *
     * @param context
     * @throws Exception
     */
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER, enabled = true)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void minDaysTest(@ArquillianResource URL context) throws Exception {
        JsonArray jsonErrors = getMetricsInvalid(context, "days=0");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "0", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 30>", null)));
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
        JsonArray jsonErrors = getMetricsInvalid(context, "days=31");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("days", "31", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer in range <1 - 30>", null)));
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
        JsonArray jsonErrors = getMetricsInvalid(context, "resolver_id=xyz");
        assertThat(jsonErrors.size(), is(1));
        assertThat(jsonErrors.get(0), is(error("resolver_id", "xyz", 21, "INVALID_PARAM_VALUE", "" +
                "Invalid value - value must be an integer", null)));
    }

    private static JsonArray getMetrics(URL context, String queryString) throws IOException {
        return AggregationUtils.getAggregationBucketsArray(context, "1/resolver/metrics?" + queryString, CLIENT_ID);
    }

    private static JsonArray getMetricsInvalid(URL context, String queryString) throws IOException {
        return InvalidRequestUtils.sendInvalidRequest(context, "1/resolver/metrics?" + queryString, CLIENT_ID);
    }

    private static ZonedDateTime timestamp() {
        return Instant.now().atZone(ZoneId.of("UTC")).minusMinutes(1);
    }

    private static String formatTimestamp(ZonedDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
    }

    private static JsonObject resolverMetrics(final int resolverId,
                                              final String hostname,
                                              final JsonArray timeline) {
        JsonObject resolverMetrics = new JsonObject();
        resolverMetrics.addProperty("resolver_id", resolverId);
        resolverMetrics.addProperty("hostname", hostname);
        resolverMetrics.add("timeline", timeline);
        return resolverMetrics;
    }

    private static JsonArray timeline(JsonObject... timelineBuckets) {
        JsonArray timeline = new JsonArray(timelineBuckets.length);
        Stream.of(timelineBuckets).forEach(timeline::add);
        return timeline;
    }

    private static JsonObject timelineBucket(final String timestamp,
                                             final JsonObject usage,
                                             final JsonObject latency,
                                             final String check) {
        JsonObject timelineBucket = new JsonObject();
        timelineBucket.addProperty("timestamp", timestamp);
        timelineBucket.add("usage", usage);
        timelineBucket.add("latency", latency);
        timelineBucket.addProperty("check", check);
        return timelineBucket;
    }

    private static JsonObject usage(final double cpuUsage,
                                    final double memoryUsage,
                                    final double hddUsage,
                                    final double swapUsage) {
        JsonObject usage = new JsonObject();
        usage.addProperty("cpu", cpuUsage);
        usage.addProperty("memory", memoryUsage);
        usage.addProperty("hdd", hddUsage);
        usage.addProperty("swap", swapUsage);
        return usage;
    }

    private static JsonObject latency(final long answers1ms,
                                      final long answers10ms,
                                      final long answers50ms,
                                      final long answers100ms,
                                      final long answers250ms,
                                      final long answers500ms,
                                      final long answers1000ms,
                                      final long answers1500ms) {
        JsonObject latency = new JsonObject();
        latency.addProperty("answers_1ms", answers1ms);
        latency.addProperty("answers_10ms", answers10ms);
        latency.addProperty("answers_50ms", answers50ms);
        latency.addProperty("answers_100ms", answers100ms);
        latency.addProperty("answers_250ms", answers250ms);
        latency.addProperty("answers_500ms", answers500ms);
        latency.addProperty("answers_1000ms", answers1000ms);
        latency.addProperty("answers_1500ms", answers1500ms);
        return latency;
    }
}
