package io.whalebone.publicapi.ejb.elastic;

import io.whalebone.publicapi.ejb.dto.DnsAggregateBucketDTO;
import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import io.whalebone.publicapi.ejb.dto.aggregate.IDnsAggregate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.joda.time.DateTime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DnsTimeBucketDTOProducer {
    private static final Logger logger = Logger.getLogger(DnsTimeBucketDTOProducer.class.getName());

    public static final String TIME_AGGREGATION = "by_time";
    public static final String TERM_AGGREGATION = "by_term";

    private final IDnsAggregate aggregateType;

    public DnsTimeBucketDTOProducer(final IDnsAggregate aggregateType) {
        this.aggregateType = aggregateType;
    }

    public List<DnsTimeBucketDTO> produce(Aggregations aggregations) {
        if (aggregations == null) {
            return Collections.emptyList();
        }
        Histogram aggregation = aggregations.get(TIME_AGGREGATION);
        List<DnsTimeBucketDTO> buckets = new ArrayList<>();
        for(Histogram.Bucket hourBucket : aggregation.getBuckets()) {
            DateTime key = (DateTime) hourBucket.getKey();
            DnsTimeBucketDTO timeBucket = DnsTimeBucketDTO.builder()
                    .count(hourBucket.getDocCount())
                    .timestamp(ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(key.getMillis()),
                            ZoneId.of(key.getZone().getID(), ZoneId.SHORT_IDS)
                            )
                    )
                    .buckets(new ArrayList<>())
                    .build();
            buckets.add(timeBucket);

            Terms termsAggregation = hourBucket.getAggregations().get(TERM_AGGREGATION);
            if (termsAggregation != null && CollectionUtils.isNotEmpty(termsAggregation.getBuckets())) {
                for (Terms.Bucket termBucket : termsAggregation.getBuckets()) {
                    DnsAggregateBucketDTO aggregateBucket = buildAggregateBucket(termBucket);
                    if (aggregateBucket != null) {
                        timeBucket.getBuckets().add(aggregateBucket);
                    }
                }
            }
            if (CollectionUtils.isEmpty(timeBucket.getBuckets())) {
                timeBucket.setBuckets(null);
            }
        }
        return buckets;
    }

    DnsAggregateBucketDTO buildAggregateBucket(Terms.Bucket termBucket) {
        if (aggregateType == null) {
            throw new IllegalStateException("Term aggregation without aggregate type is not possible");
        }
        DnsAggregateBucketDTO aggregateBucket = new DnsAggregateBucketDTO();
        aggregateBucket.setCount(termBucket.getDocCount());

        switch (aggregateType.getElasticField()) {
            case IDnsAggregate.QUERY_TYPE:
                // elastic use case insensitive search by default if there is no appropriate mapping
                // and returns aggregation keys as lowercase. For such a case we use uppercase for the key
                // to mach the enum values correctly
                try {
                    aggregateBucket.setQueryType(EDnsQueryType.valueOf(StringUtils.upperCase(termBucket.getKeyAsString())));
                } catch (IllegalArgumentException iae) {
                    logger.log(Level.FINEST, "Unknown query type value \"{0}\"", termBucket.getKey());
                    return null;
                }
                break;
            case IDnsAggregate.TLD:
                aggregateBucket.setTld(termBucket.getKeyAsString());
                break;
            case IDnsAggregate.QUERY:
                aggregateBucket.setQuery(termBucket.getKeyAsString());
                break;
            case IDnsAggregate.ANSWER:
                aggregateBucket.setAnswer(termBucket.getKeyAsString());
                break;
            case IDnsAggregate.DOMAIN:
                aggregateBucket.setDomain(termBucket.getKeyAsString());
                break;
            case IDnsAggregate.CLIENT_IP:
                aggregateBucket.setClientIp(termBucket.getKeyAsString());
                break;
            case IDnsAggregate.DEVICE_ID:
                aggregateBucket.setDeviceId(termBucket.getKeyAsString());
                break;
            default:
                throw new IllegalArgumentException("Aggregation by " + aggregateType.getElasticField() + " is not supported");
        }
        return aggregateBucket;
    }
}
