package io.whalebone.publicapi.ejb.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeAdapter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);

    @Override
    public ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String serialized = json.getAsString();
        return ZonedDateTime.parse(serialized, formatter);

    }

    @Override
    public JsonElement serialize(ZonedDateTime time, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(time.format(formatter));
    }
}
