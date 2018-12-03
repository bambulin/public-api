package io.whalebone.publicapi.ejb.elastic;

import io.whalebone.publicapi.ejb.dto.DnsAggregateBucketDTO;
import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.EAggregate;
import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DnsTimeBucketDTOProducer {
    public static final String TIME_AGGREGATION = "by_time";
    public static final String TERM_AGGREGATION = "by_term";

    private final EAggregate aggregateType;

    public DnsTimeBucketDTOProducer(final EAggregate aggregateType) {
        this.aggregateType = aggregateType;
    }

    public List<DnsTimeBucketDTO> produce(Aggregations aggregations) {
        if (aggregations == null) {
            return Collections.emptyList();
        }
        DateHistogram aggregation = aggregations.get(TIME_AGGREGATION);
        List<DnsTimeBucketDTO> buckets = new ArrayList<>();
        for(DateHistogram.Bucket hourBucket : aggregation.getBuckets()) {
            DnsTimeBucketDTO timeBucket = DnsTimeBucketDTO.builder()
                    .count(hourBucket.getDocCount())
                    .timestamp(ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(hourBucket.getKeyAsDate().getMillis()),
                            ZoneId.of(hourBucket.getKeyAsDate().getZone().getID(), ZoneId.SHORT_IDS)
                            )
                    )
                    .buckets(new ArrayList<>())
                    .build();
            buckets.add(timeBucket);

            Terms termsAggregation = hourBucket.getAggregations().get(TERM_AGGREGATION);
            if (termsAggregation != null && CollectionUtils.isNotEmpty(termsAggregation.getBuckets())) {
                for (Terms.Bucket termBucket : termsAggregation.getBuckets()) {
                    timeBucket.getBuckets().add(buildAggregateBucket(termBucket));
                }
            }
            if (CollectionUtils.isEmpty(timeBucket.getBuckets())) {
                timeBucket.setBuckets(null);
            }
        }
        return buckets;
    }

    private DnsAggregateBucketDTO buildAggregateBucket(Terms.Bucket termBucket) {
        if (aggregateType == null) {
            throw new IllegalStateException("Term aggregation without aggregate type is not possible");
        }
        DnsAggregateBucketDTO aggregateBucket = new DnsAggregateBucketDTO();
        aggregateBucket.setCount(termBucket.getDocCount());

        switch (aggregateType) {
            case TYPE:
                // elastic use case insensitive search by default if there is no appropriate mapping
                // and returns aggregation keys as lowercase. For such a case we use uppercase for the key
                // to mach the enum values correctly
                aggregateBucket.setType(EDnsQueryType.valueOf(StringUtils.upperCase(termBucket.getKey())));
                break;
            case TLD:
                aggregateBucket.setTld(termBucket.getKey());
                break;
            case QUERY:
                aggregateBucket.setQuery(termBucket.getKey());
                break;
            case ANSWER:
                aggregateBucket.setAnswer(termBucket.getKey());
                break;
            case DOMAIN:
                aggregateBucket.setDomain(termBucket.getKey());
                break;
            case CLIENT_IP:
                aggregateBucket.setClientIp(termBucket.getKey());
                break;
            default:
                throw new IllegalArgumentException("Aggregation by " + aggregateType + " is not supported");
        }
        return aggregateBucket;
    }
}
