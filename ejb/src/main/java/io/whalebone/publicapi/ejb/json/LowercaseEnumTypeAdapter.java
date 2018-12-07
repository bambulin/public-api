package io.whalebone.publicapi.ejb.json;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LowercaseEnumTypeAdapter<E extends Enum<E>> extends TypeAdapter<E> {

    private static final Logger logger = Logger.getLogger(LowercaseEnumTypeAdapter.class.getName());

    private Class<E> enumClass;
    private final Map<String, E> deserializationMap = new HashMap<>();
    private final Map<E, String> serializationMap = new HashMap<>();

    public LowercaseEnumTypeAdapter(Class<E> clazz) {
        enumClass = clazz;
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isEnumConstant()) {
                try {
                    E constant = (E) f.get(null);
                    String serializedConstant;
                    SerializedName annotation = f.getAnnotation(SerializedName.class);
                    if (annotation != null) {
                        serializedConstant = annotation.value();
                    } else {
                        serializedConstant = serializedConstant(constant);
                    }
                    serializationMap.put(constant, serializedConstant);
                    deserializationMap.put(serializedConstant, constant);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot create type adapter for " + clazz.getSimpleName(), e);
                }
            }
        }
    }

    public void write(JsonWriter out, E value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(serializationMap.get(value));
        }
    }

    public E read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        } else {
            String value = reader.nextString();
            if (!deserializationMap.containsKey(value)) {
                logger.log(Level.WARNING, "Unknown enum value \"{0}\" of enum {1}", new Object[] {value, enumClass});
                return null;
            }
            return deserializationMap.get(value);
        }
    }

    private static String serializedConstant(Object o) {
        return StringUtils.lowerCase(o.toString());
    }
}
