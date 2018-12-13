package io.whalebone.publicapi.ejb.elastic;

import io.whalebone.publicapi.ejb.dto.DnsAggregateBucketDTO;
import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.EAggregate;
import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class DnsTimeBucketDTOProducerTest {
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    @Test
    public void produceTest_aggregateByClientId() {
        List<DateHistogram.Bucket> dateHistogramBuckets = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            List<Terms.Bucket> termsBuckets = new ArrayList<>(5);
            for (int j = 0; j < 5; j++) {
                termsBuckets.add(prepareTermsBucket("10.1.1." + j, 10 + j));
            }
            dateHistogramBuckets.add(prepareDateHistogramBucket("2018-01-01T14:00:0" + i + "+0000", 5 + i, termsBuckets));
        }
        Aggregations aggregations = prepareAggregations(dateHistogramBuckets);

        DnsTimeBucketDTOProducer producer = new DnsTimeBucketDTOProducer(EAggregate.CLIENT_IP);
        List<DnsTimeBucketDTO> buckets = producer.produce(aggregations);

        assertThat(buckets, is(notNullValue()));
        assertThat(buckets, hasSize(3));
        for(int i = 0; i < buckets.size(); i++) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
            ZonedDateTime timestamp = ZonedDateTime.parse("2018-01-01T14:00:0" + i + "+0000", formatter);
            assertThat(buckets.get(i).getTimestamp().toInstant().toEpochMilli(), is(timestamp.toInstant().toEpochMilli()));
            assertThat(buckets.get(i).getCount(), is(5L + i));
            assertThat(buckets.get(i).getBuckets(), hasSize(5));
            for (int j = 0; j < buckets.get(i).getBuckets().size(); j++) {
                DnsAggregateBucketDTO aggBucket = buckets.get(i).getBuckets().get(j);
                assertThat(aggBucket.getClientIp(), is("10.1.1." + j));
                assertThat(aggBucket.getCount(), is(10L + j));
            }
        }
    }

    @Test
    public void produceTest_aggregateByQueryType() {
        List<DateHistogram.Bucket> dateHistogramBuckets = new ArrayList<>(3);
        List<String> aggregateKeys = Arrays.asList("aaaa", "loc", "CDNSKEY", "TXT", "unknown" /*this bucket should be dropped*/);
        List<EDnsQueryType> expectedQueryTypes = Arrays.asList(EDnsQueryType.AAAA, EDnsQueryType.LOC,
                EDnsQueryType.CDNSKEY, EDnsQueryType.TXT);
        for (int i = 0; i < 3; i++) {
            List<Terms.Bucket> termsBuckets = new ArrayList<>(5);
            for (int j = 0; j < 5; j++) {
                termsBuckets.add(prepareTermsBucket(aggregateKeys.get(j), 10 + j));
            }
            dateHistogramBuckets.add(prepareDateHistogramBucket("2018-01-01T14:00:0" + i + "+0000", 5 + i, termsBuckets));
        }
        Aggregations aggregations = prepareAggregations(dateHistogramBuckets);

        DnsTimeBucketDTOProducer producer = new DnsTimeBucketDTOProducer(EAggregate.TYPE);
        List<DnsTimeBucketDTO> buckets = producer.produce(aggregations);

        assertThat(buckets, is(notNullValue()));
        assertThat(buckets, hasSize(3));
        for(int i = 0; i < buckets.size(); i++) {
            assertThat(buckets.get(i).getBuckets(), hasSize(4));
            for (int j = 0; j < buckets.get(i).getBuckets().size(); j++) {
                DnsAggregateBucketDTO aggBucket = buckets.get(i).getBuckets().get(j);
                assertThat(aggBucket.getQueryType(), is(expectedQueryTypes.get(j)));
            }
        }
    }

    @Test
    public void produceTest_aggregationNotAvailable() {
        List<DateHistogram.Bucket> dateHistogramBuckets = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            dateHistogramBuckets.add(prepareDateHistogramBucket("2018-01-01T14:00:0" + i + "+0000", 5 + i, null));
        }
        Aggregations aggregations = prepareAggregations(dateHistogramBuckets);

        DnsTimeBucketDTOProducer producer = new DnsTimeBucketDTOProducer(EAggregate.TYPE);
        List<DnsTimeBucketDTO> buckets = producer.produce(aggregations);

        assertThat(buckets, is(notNullValue()));
        assertThat(buckets, hasSize(3));
        for(int i = 0; i < buckets.size(); i++) {
            assertThat(buckets.get(i).getBuckets(), is(nullValue()));
        }
    }

    @DataProvider
    public Object[][] buildAggregateBucketTestData() {
        return new Object[][] {
                new Object[] {EAggregate.CLIENT_IP, "1.2.3.4", "1.2.3.4", null, null, null, null, null},
                new Object[] {EAggregate.TYPE, "aaaa", null, EDnsQueryType.AAAA, null, null, null, null},
                new Object[] {EAggregate.TYPE, "AAAA", null, EDnsQueryType.AAAA, null, null, null, null},
                new Object[] {EAggregate.ANSWER, "answer", null, null, "answer", null, null, null},
                new Object[] {EAggregate.DOMAIN, "whalebone.io", null, null, null, "whalebone.io", null, null},
                new Object[] {EAggregate.QUERY, "some.thing.whalebone.io", null, null, null, null, "some.thing.whalebone.io", null},
                new Object[] {EAggregate.TLD, "io", null, null, null, null, null, "io"},
        };
    }

    @Test(dataProvider = "buildAggregateBucketTestData")
    public void buildAggregateBucketTest(EAggregate aggregateBy, String aggregationKey, String expectedClientIp,
                                         EDnsQueryType expectedQueryType, String expectedAnswer, String expectedDomain,
                                         String expectedQuery, String expectedTld) {
        DnsTimeBucketDTOProducer producer = new DnsTimeBucketDTOProducer(aggregateBy);
        Terms.Bucket termsBucket = prepareTermsBucket(aggregationKey, 42L);

        DnsAggregateBucketDTO bucket = producer.buildAggregateBucket(termsBucket);
        assertThat(bucket, is(notNullValue()));
        assertThat(bucket.getCount(), is(42L));
        assertThat(bucket.getClientIp(), is(expectedClientIp));
        assertThat(bucket.getQueryType(), is(expectedQueryType));
        assertThat(bucket.getAnswer(), is(expectedAnswer));
        assertThat(bucket.getDomain(), is(expectedDomain));
        assertThat(bucket.getQuery(), is(expectedQuery));
        assertThat(bucket.getTld(), is(expectedTld));
    }

    private Aggregations prepareAggregations(List<DateHistogram.Bucket> buckets) {
        DateHistogram histogram = mock(DateHistogram.class);
        doReturn(buckets).when(histogram).getBuckets();
        Aggregations aggregations = mock(Aggregations.class);
        doReturn(histogram).when(aggregations).get(DnsTimeBucketDTOProducer.TIME_AGGREGATION);
        return aggregations;
    }

    private DateHistogram.Bucket prepareDateHistogramBucket(String timestamp, long count, List<Terms.Bucket> buckets) {
        DateHistogram.Bucket bucket = mock(DateHistogram.Bucket.class);
        DateTimeFormatter formatter = DateTimeFormat.forPattern(TIMESTAMP_FORMAT);
        DateTime dateTime = DateTime.parse(timestamp, formatter);
        doReturn(dateTime).when(bucket).getKeyAsDate();
        doReturn(count).when(bucket).getDocCount();

        Aggregations aggregations = mock(Aggregations.class);
        doReturn(aggregations).when(bucket).getAggregations();

        if (CollectionUtils.isNotEmpty(buckets)) {
            Terms termsAggregation = mock(Terms.class);
            doReturn(buckets).when(termsAggregation).getBuckets();
            doReturn(termsAggregation).when(aggregations).get(DnsTimeBucketDTOProducer.TERM_AGGREGATION);
        }
        return bucket;
    }

    private Terms.Bucket prepareTermsBucket(String key, long count) {
        Terms.Bucket bucket = mock(Terms.Bucket.class);
        doReturn(count).when(bucket).getDocCount();
        doReturn(key).when(bucket).getKey();
        return bucket;
    }
}
