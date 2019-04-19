package io.whalebone.publicapi.ejb.elastic;

import io.whalebone.publicapi.ejb.dto.EDnsBucketInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BucketIntervalMapperTest {

    @DataProvider
    public Object[][] getMappedIntervalTestData() {
        return new Object[][] {
                {EDnsBucketInterval.DAY, DateHistogramInterval.DAY},
                {EDnsBucketInterval.HOUR, DateHistogramInterval.HOUR},
                {EDnsBucketInterval.WEEK, DateHistogramInterval.WEEK},
        };
    }

    @Test(dataProvider = "getMappedIntervalTestData")
    public void getMappedIntervalTest(EDnsBucketInterval interval, DateHistogramInterval expected) {
        assertThat(BucketIntervalMapper.getMappedInterval(interval), is(expected));
    }
}
