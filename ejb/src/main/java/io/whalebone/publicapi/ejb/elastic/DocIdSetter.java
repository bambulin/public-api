package io.whalebone.publicapi.ejb.elastic;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;

public class DocIdSetter {
    public static void setDocIdIfApplicable(Object doc, String docId) {
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(doc.getClass(), DocId.class);
        fields.forEach(f -> {
            f.setAccessible(true);
            try {
                f.set(doc, docId);
            } catch (IllegalAccessException iae) {
                throw new ElasticSearchException("Cannot set docId", iae);
            }
        });
    }
}
