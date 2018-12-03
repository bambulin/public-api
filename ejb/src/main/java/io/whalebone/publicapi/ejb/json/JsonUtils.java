package io.whalebone.publicapi.ejb.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class JsonUtils {
    public static JsonElement traverseJsonSingleField(JsonElement json, String fieldNotation) throws JsonParseException {
        Set<JsonElement> elements = traverseJson(json, fieldNotation);
        if (CollectionUtils.isNotEmpty(elements)) {
            if (elements.size() > 1) {
                throw new JsonParseException("Field path " + fieldNotation + " is ambiguous (possible array on the path)");
            } else {
                return elements.iterator().next();
            }
        } else {
            return null;
        }
    }

    /**
     * @param json
     * @param fieldNotation
     * @return
     */
    public static Set<JsonElement> traverseJson(JsonElement json, String fieldNotation) {
        Set<JsonElement> elements = new HashSet<>();
        String[] path = fieldNotation.split("\\.");
        JsonElement element = json;
        for (int depth = 0; depth < path.length; depth++) {
            if (element.isJsonObject()) {
                element = element.getAsJsonObject().get(path[depth]);
                if (element == null) {
                    return Collections.emptySet();
                }
                if (depth >= path.length - 1) {
                    elements.add(element);
                }
            } else if (element.isJsonArray()) {
                JsonArray jsonArray = element.getAsJsonArray();
                for (JsonElement arrayElement : jsonArray) {
                    elements.addAll(
                            traverseJson(
                                    arrayElement,
                                    String.join(".", Arrays.copyOfRange(path, depth, path.length))
                            )
                    );
                }
                break;
            } else {
                return Collections.emptySet();
            }
        }
        return elements;
    }
}
