package io.whalebone.publicapi.tests.matchers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.whalebone.publicapi.tests.MatchException;
import lombok.Setter;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Setter
public abstract class JsonElementMatcher extends TypeSafeMatcher<JsonElement> {
    private String failedPropertyName;
    private Object expectedValue;
    private Object gotValue;
    private boolean regexp;

    @Override
    public void describeTo(Description description) {
        if (regexp) {
            description.appendText("expected to have a property '" + failedPropertyName + "' that matches regexp: ").appendValue(expectedValue);
        } else {
            description.appendText("expected to have a property '" + failedPropertyName + "': ").appendValue(expectedValue);
        }
    }

    @Override
    protected void describeMismatchSafely(JsonElement item, Description mismatchDescription) {
        mismatchDescription.appendText("was ").appendValue(gotValue);
    }

    void checkNullableProperty(JsonObject event, String propertyName, Object property, Function<JsonElement,
            Object> getter) throws MatchException {
        if (property != null) {
            if (event.get(propertyName) == null || event.get(propertyName).isJsonNull()) {
                failedPropertyName = propertyName;
                expectedValue = property;
                gotValue = null;
                throw new MatchException();
            } else if (!property.equals(getter.apply(event.get(propertyName)))) {
                failedPropertyName = propertyName;
                expectedValue = property;
                gotValue = getter.apply(event.get(propertyName));
                throw new MatchException();
            }
        } else if (event.get(propertyName) != null && !event.get(propertyName).isJsonNull()) {
            failedPropertyName = propertyName;
            expectedValue = null;
            gotValue = getter.apply(event.get(propertyName));
            throw new MatchException();
        }
    }

    void checkNullableStringArrayProperty(JsonObject event, String propertyName, String[] property)
            throws MatchException {
        if (property != null && property.length > 0) {
            if (event.get(propertyName) == null || event.get(propertyName).isJsonNull()) {
                failedPropertyName = propertyName;
                expectedValue = "array";
                gotValue = null;
                throw new MatchException();
            } else if (!event.get(propertyName).isJsonArray()) {
                failedPropertyName = propertyName;
                expectedValue = "array";
                gotValue = "not array";
                throw new MatchException();
            } else {
                JsonArray jsonArray = event.getAsJsonArray(propertyName);
                if (jsonArray.size() != property.length) {
                    failedPropertyName = propertyName;
                    expectedValue = "array of length " + property.length;
                    gotValue = "array of length " + jsonArray.size();
                    throw new MatchException();
                } else {
                    List<String> list = StreamSupport.stream(jsonArray.spliterator(), false).map(JsonElement::getAsString).collect(Collectors.toList());
                    List<String> expectedList = Arrays.asList(property);
                    if (!expectedList.containsAll(list)) {
                        failedPropertyName = propertyName;
                        expectedValue = expectedList;
                        gotValue = list;
                        throw new MatchException();
                    }
                }
            }
        }
    }
}
