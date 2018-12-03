package io.whalebone.publicapi.ejb.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.whalebone.publicapi.ejb.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.testng.Assert.assertTrue;


public class JsonUtilsTest {

    private static JsonElement json;

    @BeforeClass
    public static void initClass() throws Exception{
        json = new JsonParser().parse(FileUtils.resourceFileAsString("io/whalebone/publicapi/ejb/json/json-utils.json"));
    }

    @Test
    public void traverseJson_primitiveField() {

        Set<JsonElement> traversedSet = JsonUtils.traverseJson(json, "field1");

        assertThat(traversedSet, is(notNullValue()));
        assertThat(traversedSet, hasSize(1));
        assertThat(traversedSet.iterator().next().getAsString(), is("value1"));
    }

    @Test
    public void traverseJson_fieldOfInnerObject() {

        Set<JsonElement> traversedSet = JsonUtils.traverseJson(json, "innerObject1.field2");

        assertThat(traversedSet, is(notNullValue()));
        assertThat(traversedSet, hasSize(1));
        assertThat(traversedSet.iterator().next().getAsString(), is("value2"));
    }

    @Test
    public void traverseJson_arrayOfPrimitives() {

        Set<JsonElement> traversedSet = JsonUtils.traverseJson(json, "array1");

        assertThat(traversedSet, is(notNullValue()));
        assertThat(traversedSet, hasSize(1));
        assertTrue(traversedSet.iterator().next().isJsonArray());
    }

    @Test
    public void traverseJson_arrayOfFieldsOfInnerObjects() {

        Set<JsonElement> traversedSet = JsonUtils.traverseJson(json, "arrayOfObjects.field4");

        assertThat(traversedSet, is(notNullValue()));
        assertThat(traversedSet, hasSize(2));
        int i = 0;
        for(JsonElement element : traversedSet) {
            assertTrue(element.isJsonPrimitive());
            assertThat(element.getAsString(), is("value" + (4 + i * 6)));
            i++;
        }
    }

    @Test
    public void traverseJson_filedObjectOfInnerObject() {
        Set<JsonElement> traversedSet = JsonUtils.traverseJson(json, "innerObject2.innerObject3");

        assertThat(traversedSet, is(notNullValue()));
        assertThat(traversedSet, hasSize(1));
        assertTrue(traversedSet.iterator().next().isJsonObject());
    }

    @Test
    public void traverseJsonSingleField() {
        JsonElement element = JsonUtils.traverseJsonSingleField(json, "innerObject1.field2");

        assertThat(element, is(notNullValue()));
        assertTrue(element.isJsonPrimitive());
        assertThat(element.getAsString(), is("value2"));
    }

    @Test(expectedExceptions = JsonParseException.class)
    public void traverseJsonSingleField_valueIsInArray() {
        JsonUtils.traverseJsonSingleField(json, "arrayOfObjects.field3");
    }

    @DataProvider
    public Object[] missingFields() {
        return new Object[] {"notExistingField", "arrayOfObjects.notExistingField", "innerObject1.notExistingField"};
    }

    @Test(dataProvider = "missingFields")
    public void traverseJson_notExistingField(String missingFieldName) {
        Set<JsonElement> traversedSet = JsonUtils.traverseJson(json, missingFieldName);

        assertThat(traversedSet, is(notNullValue()));
        assertThat(traversedSet, hasSize(0));
    }

    @Test(dataProvider = "missingFields")
    public void traverseJsonSingleField_notExistingField(String missingFieldName) {
        JsonElement element = JsonUtils.traverseJsonSingleField(json, missingFieldName);

        assertThat(element, is(nullValue()));
    }
}

