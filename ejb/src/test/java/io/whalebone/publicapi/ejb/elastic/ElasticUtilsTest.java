package io.whalebone.publicapi.ejb.elastic;

import io.whalebone.publicapi.ejb.PublicApiService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

public class ElasticUtilsTest {
    @DataProvider
    public Object[][] testData() {
        return new Object[][] {
                // within one year
                {
                    PublicApiService.LOGS_INDEX_PREFIX, PublicApiService.LOGS_INDEX_TIME_FORMAT,
                    ZonedDateTime.of(2019, 9, 21, 15, 30, 13, 0, ZoneId.systemDefault()),
                    ZonedDateTime.of(2019, 12, 10, 1, 0, 1, 0, ZoneId.systemDefault()),
                    new String[] {
                            "logs-2019-09-*", "logs-2019-10-*", "logs-2019-11-*", "logs-2019-12-*"
                    }
                },
                // end of the year and the begining of the next one
                {
                        PublicApiService.DNSSEC_INDEX_PREFIX, PublicApiService.DNSSEC_INDEX_TIME_FORMAT,
                        ZonedDateTime.of(2019, 11, 21, 15, 30, 13, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2020, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        new String[] {
                                "dnssec-2019.11.*", "dnssec-2019.12.*", "dnssec-2020.01.*", "dnssec-2020.02.*"
                        }
                },
                // within one month
                {
                        PublicApiService.PASSIVE_DNS_INDEX_PREFIX, PublicApiService.PASSIVE_DNS_INDEX_TIME_FORMAT,
                        ZonedDateTime.of(2019, 10, 13, 4, 22, 48, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2019, 10, 25, 1, 0, 0, 0, ZoneId.systemDefault()),
                        new String[] {"passivedns-2019.10.*"}
                },
                // same time value
                {
                        PublicApiService.PASSIVE_DNS_INDEX_PREFIX, PublicApiService.PASSIVE_DNS_INDEX_TIME_FORMAT,
                        ZonedDateTime.of(2019, 10, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2019, 10, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        new String[] {"passivedns-2019.10.*"}
                },
                // 1 nanosec difference
                {
                        PublicApiService.PASSIVE_DNS_INDEX_PREFIX, PublicApiService.PASSIVE_DNS_INDEX_TIME_FORMAT,
                        ZonedDateTime.of(2019, 10, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2019, 10, 1, 0, 0, 0, 1, ZoneId.systemDefault()),
                        new String[] {"passivedns-2019.10.*"}
                },
                // 1 nanosec difference
                {
                        PublicApiService.PASSIVE_DNS_INDEX_PREFIX, PublicApiService.PASSIVE_DNS_INDEX_TIME_FORMAT,
                        ZonedDateTime.of(2019, 9, 30, 23, 59, 59, 999999999, ZoneId.systemDefault()),
                        ZonedDateTime.of(2019, 10, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        new String[] {"passivedns-2019.09.*", "passivedns-2019.10.*"}
                }
        };
    }

    @Test(dataProvider = "testData")
    public void indicesByMonthsTest(String indexPrefix, String indexTimePattern, ZonedDateTime from, ZonedDateTime to,
                                    String[] expectedIndices) {
        String[] indices = ElasticUtils.indicesByMonths(indexPrefix, indexTimePattern, from, to);

        assertArrayEquals(indices, expectedIndices);
    }
}
