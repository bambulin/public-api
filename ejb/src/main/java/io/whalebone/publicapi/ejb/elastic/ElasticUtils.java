package io.whalebone.publicapi.ejb.elastic;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ElasticUtils {
    public static String[] indicesByMonths(String indexPrefix, String pattern, ZonedDateTime from, ZonedDateTime to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        List<String> indices = new ArrayList<>();
        ZonedDateTime indexTime = ZonedDateTime.of(from.getYear(), from.getMonthValue(), 1, 0, 0, 0, 0, from.getZone());
        String index;
        do {
            index = indexPrefix + indexTime.format(formatter);
            indices.add(index);
            indexTime = indexTime.plusMonths(1);
        } while (indexTime.isBefore(to) || indexTime.equals(to));
        return indices.toArray(new String[0]);
    }
}
