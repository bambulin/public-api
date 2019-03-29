package io.whalebone.publicapi.tests.utils;

import io.whalebone.publicapi.tests.ArchiveInitiator;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

public class TestDataLoader {
    @Test
    public void load() throws IOException {
        ArchiveInitiator archiveInitiator = new ArchiveInitiator();
        ZonedDateTime timestamp = timestamp();
        archiveInitiator.sendMultipleDnsLogs("passivedns/by_query_type", timestamp);
        archiveInitiator.sendDnsLog("passivedns/by_query_type/older/passivedns-query_type-a_older.json", timestamp.minusMinutes(2 * 24 * 60 + 1));
    }

    private ZonedDateTime timestamp() {
        return ZonedDateTime.now().minusMinutes(1);
    }
}
