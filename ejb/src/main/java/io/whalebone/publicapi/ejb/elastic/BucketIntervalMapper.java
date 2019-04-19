package io.whalebone.publicapi.ejb.elastic;

import io.whalebone.publicapi.ejb.dto.EDnsBucketInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;

import java.util.HashMap;
import java.util.Map;

public class BucketIntervalMapper {
    private static Map<EDnsBucketInterval, DateHistogramInterval> map = new HashMap<>();

    static {
        map.put(EDnsBucketInterval.HOUR, DateHistogramInterval.HOUR);
        map.put(EDnsBucketInterval.DAY, DateHistogramInterval.DAY);
        map.put(EDnsBucketInterval.WEEK, DateHistogramInterval.WEEK);
    }

    public static DateHistogramInterval getMappedInterval(EDnsBucketInterval interval) {
        if (!map.containsKey(interval)) {
            throw new IllegalArgumentException(interval + " is not supported yet");
        }
        return map.get(interval);
    }
}
