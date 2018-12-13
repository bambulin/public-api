package io.whalebone.publicapi.ejb.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.whalebone.publicapi.ejb.FileUtils;
import io.whalebone.publicapi.ejb.dto.EReason;
import io.whalebone.publicapi.ejb.dto.EThreatType;
import io.whalebone.publicapi.ejb.dto.EventDTO;
import io.whalebone.publicapi.ejb.dto.GeoIpDTO;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;

public class ArchiveMappedDeserializerTest {

    private Gson gson;

    public ArchiveMappedDeserializerTest() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(EventDTO.class, new ArchiveMappedDeserializer())
                .registerTypeAdapter(GeoIpDTO.class, new ArchiveMappedDeserializer())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
                .registerTypeAdapter(EReason.class, new LowercaseEnumTypeAdapter<>(EReason.class))
                .registerTypeAdapter(EThreatType.class, new LowercaseEnumTypeAdapter<>(EThreatType.class))
                .create();
    }

    @Test
    public void deserializeTest() throws IOException {
        String json = json = FileUtils.resourceFileAsString("io/whalebone/publicapi/ejb/json/event.json");
        EventDTO event = gson.fromJson(json, EventDTO.class);
        assertThat(event, is(notNullValue()));
        assertThat(event.getReason(), is(EReason.ACCURACY));
        assertThat(event.getAction(), is("block"));
        assertThat(event.getAccuracy(), is(-30));
        assertThat(event.getClientIp(), is("172.16.11.78"));
        assertThat(event.getDomain(), is("imreallybof.com"));
        assertThat(event.getResolverId(), is(123));
        assertThat(event.getTimestamp(), is(ZonedDateTime.of(2018, 11, 22, 11, 45, 13, 0, ZoneId.of("+01:00"))));
        assertThat(event.getIdentifier(), is(arrayWithSize(2)));
        assertThat(event.getIdentifier(), is(arrayContaining("identifier", "identifier2")));
        assertThat(event.getThreatType(), is(arrayWithSize(2)));
        assertThat(event.getThreatType(), is(arrayContaining(EThreatType.PHISHING, EThreatType.MALWARE)));
        assertThat(event.getGeoIp(), is(notNullValue()));
        assertThat(event.getGeoIp().getLatitude(), is(39.56450000000001));
        assertThat(event.getGeoIp().getLongitude(), is(-75.597));
        assertThat(event.getGeoIp().getCountryCode2(), is("US"));
    }
}
