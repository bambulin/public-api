package io.whalebone.publicapi.rest;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.whalebone.publicapi.ejb.dto.EAggregate;
import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import io.whalebone.publicapi.ejb.dto.EReason;
import io.whalebone.publicapi.ejb.dto.EThreatType;
import io.whalebone.publicapi.ejb.json.LowercaseEnumTypeAdapter;
import io.whalebone.publicapi.ejb.json.ZonedDateTimeAdapter;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.time.ZonedDateTime;

@Dependent
public class Resources {

    @Produces
    @Api
    public Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
                .registerTypeAdapter(EAggregate.class, new LowercaseEnumTypeAdapter<>(EAggregate.class))
                .registerTypeAdapter(EReason.class, new LowercaseEnumTypeAdapter<>(EReason.class))
                .registerTypeAdapter(EThreatType.class, new LowercaseEnumTypeAdapter<>(EThreatType.class))
                .registerTypeAdapter(EDnsQueryType.class, new LowercaseEnumTypeAdapter<>(EDnsQueryType.class))
                .create();
    }
}
