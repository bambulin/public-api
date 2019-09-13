package io.whalebone.publicapi.rest.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.whalebone.publicapi.ejb.dto.ActiveIoCStatsDTO;
import io.whalebone.publicapi.ejb.dto.EThreatType;
import org.apache.commons.collections4.MapUtils;

import java.lang.reflect.Type;
import java.util.Map;

public class ActiveIoCStatsDTOAdapter implements JsonSerializer<ActiveIoCStatsDTO> {

    @Override
    public JsonElement serialize(ActiveIoCStatsDTO activeIoCStatsDTO, Type type, JsonSerializationContext ctx) {
        JsonObject json = new JsonObject();
        json.addProperty("total", activeIoCStatsDTO.getTotalCount());
        if (MapUtils.isNotEmpty(activeIoCStatsDTO.getCountsPerTypeMap())) {
            for (Map.Entry<EThreatType, Long> typeCount : activeIoCStatsDTO.getCountsPerTypeMap().entrySet()) {
                // this call use LowerCaseEnumTypeAdapter internally so the EThreadType value is serialized correctly
                JsonElement typeJson = ctx.serialize(typeCount.getKey());
                String typeString = typeJson.getAsString();
                json.addProperty(typeString, typeCount.getValue());
            }
        }
        return json;
    }
}
