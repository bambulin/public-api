package io.whalebone.publicapi.ejb.elastic;

import io.whalebone.publicapi.ejb.dto.ETimeInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;

import java.util.HashMap;
import java.util.Map;

public class BucketIntervalMapper {
    private static Map<ETimeInterval, DateHistogramInterval> map = new HashMap<>();

    static {
        map.put(ETimeInterval.HOUR, DateHistogramInterval.HOUR);
        map.put(ETimeInterval.DAY, DateHistogramInterval.DAY);
        map.put(ETimeInterval.WEEK, DateHistogramInterval.WEEK);
    }

    public static DateHistogramInterval getMappedInterval(ETimeInterval interval) {
        if (!map.containsKey(interval)) {
            throw new IllegalArgumentException(interval + " is not supported yet");
        }
        return map.get(interval);
    }
}
