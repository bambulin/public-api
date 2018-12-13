package io.whalebone.publicapi.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EventMatcher extends TypeSafeMatcher<JsonElement> {
    private String timestamp;
    private Integer accuracy;
    private Integer resolverId;
    private String action;
    private String reason;
    private String clientIp;
    private String domain;
    private String[] threatType;
    private String[] identifier;
    private Double latitude;
    private Double longitude;
    private String countryCode2;

    private String failedPropertyName;
    private Object expectedValue;
    private Object gotValue;


    public EventMatcher(String timestamp,
                        Integer accuracy,
                        Integer resolverId,
                        String action,
                        String reason,
                        String clientIp,
                        String domain,
                        String[] threatType,
                        String[] identifier,
                        Double latitude,
                        Double longitude,
                        String countryCode2) {
        this.timestamp = timestamp;
        this.accuracy = accuracy;
        this.resolverId = resolverId;
        this.action = action;
        this.reason = reason;
        this.clientIp = clientIp;
        this.domain = domain;
        this.threatType = threatType;
        this.identifier = identifier;
        this.latitude = latitude;
        this.longitude = longitude;
        this.countryCode2 = countryCode2;
    }

    @Override
    protected boolean matchesSafely(JsonElement eventElement) {
        if (!eventElement.isJsonObject()) {
            failedPropertyName = "event";
            expectedValue = "JsonObject";
            gotValue = eventElement.getClass().getSimpleName();
            return false;
        }
        JsonObject event = eventElement.getAsJsonObject();

        // TODO fix loading id to eventDTO
//        if (event.get("event_id") == null || event.get("event_id").isJsonNull()) {
//            failedPropertyName = "event_id";
//            expectedValue = "not null";
//            gotValue = null;
//            return false;
//        }
        try {
            checkNullableProperty(event, "timestamp", timestamp, JsonElement::getAsString);
            checkNullableProperty(event, "accuracy", accuracy, JsonElement::getAsInt);
            checkNullableProperty(event, "resolver_id", resolverId, JsonElement::getAsInt);
            checkNullableProperty(event, "action", action, JsonElement::getAsString);
            checkNullableProperty(event, "reason", reason, JsonElement::getAsString);
            checkNullableProperty(event, "client_ip", clientIp, JsonElement::getAsString);
            checkNullableProperty(event, "domain", domain, JsonElement::getAsString);

            if (latitude != null || longitude != null || countryCode2 != null) {
                if (event.getAsJsonObject("geo_ip") == null || event.getAsJsonObject("geo_ip").isJsonNull()) {
                    failedPropertyName = "geo_ip";
                    expectedValue = "not null";
                    gotValue = null;
                    return false;
                }
                checkNullableProperty(event.getAsJsonObject("geo_ip"), "latitude", latitude, JsonElement::getAsDouble);
                checkNullableProperty(event.getAsJsonObject("geo_ip"), "longitude", longitude, JsonElement::getAsDouble);
                checkNullableProperty(event.getAsJsonObject("geo_ip"), "country_code2", countryCode2, JsonElement::getAsString);
            } else if (event.getAsJsonObject("geo_ip") != null && !event.getAsJsonObject("geo_ip").isJsonNull()) {
                failedPropertyName = "geo_ip";
                expectedValue = null;
                gotValue = "not null";
                return false;
            }

            checkNullableStringArrayProperty(event, "threat_type", threatType);
            checkNullableStringArrayProperty(event, "identifier", identifier);
        } catch (MatchException e) {
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(failedPropertyName + " ").appendValue(expectedValue);
    }

    @Override
    protected void describeMismatchSafely(JsonElement item, Description mismatchDescription) {
        mismatchDescription.appendText("was ").appendValue(gotValue);
    }

    private void checkNullableProperty(JsonObject event, String propertyName, Object property, Function<JsonElement,
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

    private void checkNullableStringArrayProperty(JsonObject event, String propertyName, String[] property)
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
