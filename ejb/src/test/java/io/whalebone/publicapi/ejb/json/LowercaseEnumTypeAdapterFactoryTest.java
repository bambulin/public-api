package io.whalebone.publicapi.ejb.json;

import com.google.gson.TypeAdapter;
import io.whalebone.publicapi.ejb.dto.EThreatType;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNull;

public class LowercaseEnumTypeAdapterFactoryTest {

    private TypeAdapter<EThreatType> adapter;

    public LowercaseEnumTypeAdapterFactoryTest() {
        adapter = new LowercaseEnumTypeAdapter<>(EThreatType.class);
    }

    @Test
    public void deserializationTest() throws Exception {
        EThreatType threadType = adapter.fromJson("\"blacklist\"");

        assertThat(threadType, is(EThreatType.BLACKLIST));
    }

    @Test
    public void deserializationUsingAnnotationTest() throws Exception {
        EThreatType threadType = adapter.fromJson("\"c&c\"");

        assertThat(threadType, is(EThreatType.C_AND_C));
    }

    @Test
    public void deserializationUnknownConstantTest() throws Exception {
        EThreatType threatType = adapter.fromJson("\"unknown\"");

        assertNull(threatType);
    }

    @Test
    public void serializationTest() {
        String serializedValue = adapter.toJson(EThreatType.BLACKLIST);

        assertThat(serializedValue, is("\"blacklist\""));
    }

    @Test
    public void serializationUsinAnnotationTest() {
        String serializedValue = adapter.toJson(EThreatType.C_AND_C);

        assertThat(serializedValue, is("\"c&c\""));
    }
}
