package io.whalebone.publicapi.ejb.json;

import com.google.gson.TypeAdapter;
import io.whalebone.publicapi.ejb.dto.EThreadType;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LowercaseEnumTypeAdapterFactoryTest {

    private TypeAdapter<EThreadType> adapter;

    public LowercaseEnumTypeAdapterFactoryTest() {
        adapter = new LowercaseEnumTypeAdapter<>(EThreadType.class);
    }

    @Test
    public void deserializationTest() throws Exception {
        EThreadType threadType = adapter.fromJson("\"blacklist\"");

        assertThat(threadType, is(EThreadType.BLACKLIST));
    }

    @Test
    public void deserializationUsingAnnotationTest() throws Exception {
        EThreadType threadType = adapter.fromJson("\"c&c\"");

        assertThat(threadType, is(EThreadType.C_AND_C));
    }

    @Test(expectedExceptions = IOException.class)
    public void deserializationUnknownConstantTest() throws Exception {
        adapter.fromJson("\"unknown\"");
    }

    @Test
    public void serializationTest() {
        String serializedValue = adapter.toJson(EThreadType.BLACKLIST);

        assertThat(serializedValue, is("\"blacklist\""));
    }

    @Test
    public void serializationUsinAnnotationTest() {
        String serializedValue = adapter.toJson(EThreadType.C_AND_C);

        assertThat(serializedValue, is("\"c&c\""));
    }
}
