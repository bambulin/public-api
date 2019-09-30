package io.whalebone.publicapi.ejb.elastic.producer;

import io.whalebone.publicapi.ejb.dto.ResolverMetricsDTO;
import io.whalebone.publicapi.ejb.dto.ResolverMetricsTimeBucketDTO;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.scripted.ScriptedMetric;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResolverMetricsDTOProducer {
    public static final String RESOLVER_ID_AGG = "by_resolver_id";
    public static final String HOSTNAME_AGG = "hit_for_hostname";
    public static final String HOSTNAME_FIELD = "hostname";
    public static final String HISTOGRAM_AGG = "by_time";
    public static final String CPU_USAGE_AGG = "cpu_usage";
    public static final String MEM_USAGE_AGG = "mem_usage";
    public static final String HDD_USAGE_AGG = "hdd_usage";
    public static final String SWAP_USAGE_AGG = "swap_usage";
    public static final String CHECK_AGG = "check";
    public static final String ANSWER_1MS_AGG = "resolver_answer_1ms";
    public static final String ANSWER_10MS_AGG = "resolver_answer_10ms";
    public static final String ANSWER_50MS_AGG = "resolver_answer_50ms";
    public static final String ANSWER_100MS_AGG = "resolver_answer_100ms";
    public static final String ANSWER_250MS_AGG = "resolver_answer_250ms";
    public static final String ANSWER_500MS_AGG = "resolver_answer_500ms";
    public static final String ANSWER_1000MS_AGG = "resolver_answer_1000ms";
    public static final String ANSWER_1500MS_AGG = "resolver_answer_1500ms";

    public static List<ResolverMetricsDTO> produce(Aggregations aggregations) {
        Terms byResolverIdAgg = aggregations.get(RESOLVER_ID_AGG);
        List<ResolverMetricsDTO> resolverMetricsList = new ArrayList<>();
        ResolverMetricsDTO resolverMetrics;
        for (Terms.Bucket resolverBucket : byResolverIdAgg.getBuckets()) {
            resolverMetrics = new ResolverMetricsDTO();
            resolverMetricsList.add(resolverMetrics);
            // resolver Id
            resolverMetrics.setResolverId(resolverBucket.getKeyAsNumber().intValue());

            // hostname
            TopHits topHits = resolverBucket.getAggregations().get(HOSTNAME_AGG);
            String hostname = extractHostname(topHits);
            resolverMetrics.setHostname(hostname);

            // timeline buckets
            Histogram byTimeAgg = resolverBucket.getAggregations().get(HISTOGRAM_AGG);
            List<ResolverMetricsTimeBucketDTO> timeline = extractTimeline(byTimeAgg);
            resolverMetrics.setTimeline(timeline);
        }
        return resolverMetricsList;
    }

    private static String extractHostname(TopHits topHits) {
        if (topHits.getHits().getHits().length == 0) {
            throw new IllegalStateException("No top hits returned for resolver metrics");
        }
        SearchHit hostnameHit = topHits.getHits().getAt(0);
        Map<String, Object> hitSource = hostnameHit.getSourceAsMap();
        if (!hitSource.containsKey(HOSTNAME_FIELD)) {
            throw new IllegalStateException("No " + HOSTNAME_FIELD + " set for resolver doc [doc id: " + hostnameHit.getId() + "]");
        }
        return (String) hitSource.get(HOSTNAME_FIELD);
    }

    private static List<ResolverMetricsTimeBucketDTO> extractTimeline(Histogram byTimeAggregation) {
        List<ResolverMetricsTimeBucketDTO> resolverTimeBucketList = new ArrayList<>();
        ResolverMetricsTimeBucketDTO resolverTimeBucket;
        for (Histogram.Bucket timeBucket : byTimeAggregation.getBuckets()) {
            resolverTimeBucket = new ResolverMetricsTimeBucketDTO();
            resolverTimeBucketList.add(resolverTimeBucket);
            DateTime key = (DateTime) timeBucket.getKey();
            ZonedDateTime timestamp = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(key.getMillis()),
                    ZoneId.of(key.getZone().getID(), ZoneId.SHORT_IDS)
            );
            resolverTimeBucket.setTimestamp(timestamp);

            resolverTimeBucket.setUsage(extractUsage(timeBucket));
            resolverTimeBucket.setLatency(extractLatency(timeBucket));
            resolverTimeBucket.setCheck(extractCheckAggregation(timeBucket));
        }
        return resolverTimeBucketList;
    }

    private static ResolverMetricsTimeBucketDTO.Usage extractUsage(MultiBucketsAggregation.Bucket bucket) {
        ResolverMetricsTimeBucketDTO.Usage usage = new ResolverMetricsTimeBucketDTO.Usage();
        usage.setCpu(extractAvgAggregation(bucket, CPU_USAGE_AGG));
        usage.setHdd(extractAvgAggregation(bucket, HDD_USAGE_AGG));
        usage.setSwap(extractAvgAggregation(bucket, SWAP_USAGE_AGG));
        usage.setMemory(extractAvgAggregation(bucket, MEM_USAGE_AGG));
        return usage;
    }

    private static ResolverMetricsTimeBucketDTO.Latency extractLatency(MultiBucketsAggregation.Bucket bucket) {
        ResolverMetricsTimeBucketDTO.Latency latency = new ResolverMetricsTimeBucketDTO.Latency();
        latency.setAnswers1ms(extractSumAggregation(bucket, ANSWER_1MS_AGG));
        latency.setAnswers10ms(extractSumAggregation(bucket, ANSWER_10MS_AGG));
        latency.setAnswers50ms(extractSumAggregation(bucket, ANSWER_50MS_AGG));
        latency.setAnswers100ms(extractSumAggregation(bucket, ANSWER_100MS_AGG));
        latency.setAnswers250ms(extractSumAggregation(bucket, ANSWER_250MS_AGG));
        latency.setAnswers500ms(extractSumAggregation(bucket, ANSWER_500MS_AGG));
        latency.setAnswers1000ms(extractSumAggregation(bucket, ANSWER_1000MS_AGG));
        latency.setAnswers1500ms(extractSumAggregation(bucket, ANSWER_1500MS_AGG));
        return latency;
    }

    private static double extractAvgAggregation(MultiBucketsAggregation.Bucket bucket, String aggName) {
        Avg avgAgg = bucket.getAggregations().get(aggName);
        BigDecimal rounded = BigDecimal.valueOf(avgAgg.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
        return rounded.doubleValue();
    }

    private static long extractSumAggregation(MultiBucketsAggregation.Bucket bucket, String aggName) {
        Sum sumAgg = bucket.getAggregations().get(aggName);
        // integers only are expected to be summed so double is not necessary
        return Math.round(sumAgg.getValue());
    }

    private static String extractCheckAggregation(MultiBucketsAggregation.Bucket bucket) {
        ScriptedMetric sumAgg = bucket.getAggregations().get(CHECK_AGG);
        boolean check = (boolean) sumAgg.aggregation();
        return check ? "ok" : "fail";
    }
}
