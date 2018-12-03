package io.whalebone.publicapi.rest;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EnumParamUtils {
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E getEnumValue(Class<E> enumClass, String paramValue) {
        if (!enumClass.isEnum()) {
            return null;
        }
        try {
            for (Field f : enumClass.getFields()) {
                if (StringUtils.equals(getNormalizedEnumConstantName(f), StringUtils.lowerCase(paramValue))) {
                    return (E) f.get(null);
                }
            }
        } catch (IllegalAccessException e) {
            return null;
        }
        return null;
    }

    private static String getNormalizedEnumConstantName(Field f) {
        if (f.isEnumConstant()) {
            SerializedName a = f.getAnnotation(SerializedName.class);
            String constantNormalizedName;
            if (a != null) {
                constantNormalizedName = a.value();
            } else {
                constantNormalizedName = StringUtils.lowerCase(f.getName());
            }
            return constantNormalizedName;
        } else {
            return null;
        }
    }

    public static List<String> getEnumValuesForParam(Class enumClass) {
        List<String> values = new ArrayList<>();
        if (enumClass.isEnum()) {
            for (Field f : enumClass.getFields()) {
                String normalizedName = getNormalizedEnumConstantName(f);
                if (StringUtils.isNotBlank(normalizedName)) {
                    values.add(normalizedName);
                }
            }
        }
        return values;
    }
}
