package io.whalebone.publicapi.ejb.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Set;

public class ArchiveMappedDeserializer implements JsonDeserializer<ArchiveMapped> {

    @Override
    public ArchiveMapped deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        Class<ArchiveMapped> clazz = (Class<ArchiveMapped>) typeOfT;
        ArchiveMapped bean;
        try {
            bean = ConstructorUtils.invokeConstructor(clazz);
            Field[] fields = FieldUtils.getFieldsWithAnnotation(clazz, ArchiveMappedField.class);
            for (Field f : fields) {
                Object value = null;
                String fieldMapping = f.getAnnotation(ArchiveMappedField.class).value();
                if (f.getType().isArray()) {
                    Set<JsonElement> elements = JsonUtils.traverseJson(json, fieldMapping);
                    if (CollectionUtils.isNotEmpty(elements)) {
                        Object array = Array.newInstance(f.getType().getComponentType(), elements.size());
                        int i = 0;
                        for (JsonElement element : elements) {
                            Object deserialized = context.deserialize(element, f.getType().getComponentType());
                            Array.set(array, i, deserialized);
                            i++;
                        }
                        f.setAccessible(true);
                        f.set(bean, array);
                    }
                } else {
                    JsonElement element = JsonUtils.traverseJsonSingleField(json, fieldMapping);
                    value = context.deserialize(element, f.getType());
                    f.setAccessible(true);
                    f.set(bean, value);
                }
            }
            return bean;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException  e) {
            throw new JsonParseException("Cannot deserialize " + clazz.getSimpleName(), e);
        }
    }
}
