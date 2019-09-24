package io.whalebone.publicapi.tests.matchers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.whalebone.publicapi.tests.MatchException;

public class EventMatcher extends JsonElementMatcher {
    private String timestamp;
    private Integer accuracy;
    private Integer resolverId;
    private String action;
    private String reason;
    private String clientIp;
    private String domain;
    private String deviceId;
    private String[] threatType;
    private String[] identifier;
    private Double latitude;
    private Double longitude;
    private String countryCode2;

    public EventMatcher(String timestamp,
                        Integer accuracy,
                        Integer resolverId,
                        String action,
                        String reason,
                        String clientIp,
                        String domain,
                        String deviceId,
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
        this.deviceId = deviceId;
        this.threatType = threatType;
        this.identifier = identifier;
        this.latitude = latitude;
        this.longitude = longitude;
        this.countryCode2 = countryCode2;
    }

    @Override
    protected boolean matchesSafely(JsonElement eventElement) {
        if (!eventElement.isJsonObject()) {
            setFailedPropertyName("json");
            setExpectedValue("JsonObject");
            setGotValue(eventElement.getClass().getSimpleName());
            return false;
        }
        JsonObject event = eventElement.getAsJsonObject();

        if (event.get("event_id") == null || event.get("event_id").isJsonNull()) {
            setFailedPropertyName("event_id");
            setExpectedValue("not null");
            setGotValue(null);
            return false;
        }

        try {
            checkNullableProperty(event, "timestamp", timestamp, JsonElement::getAsString);
            checkNullableProperty(event, "accuracy", accuracy, JsonElement::getAsInt);
            checkNullableProperty(event, "resolver_id", resolverId, JsonElement::getAsInt);
            checkNullableProperty(event, "action", action, JsonElement::getAsString);
            checkNullableProperty(event, "reason", reason, JsonElement::getAsString);
            checkNullableProperty(event, "client_ip", clientIp, JsonElement::getAsString);
            checkNullableProperty(event, "domain", domain, JsonElement::getAsString);
            checkNullableProperty(event, "device_id", deviceId, JsonElement::getAsString);

            if (latitude != null || longitude != null || countryCode2 != null) {
                if (event.getAsJsonObject("geo_ip") == null || event.getAsJsonObject("geo_ip").isJsonNull()) {
                    setFailedPropertyName("geo_ip");
                    setExpectedValue("not null");
                    setGotValue(null);
                    return false;
                }
                checkNullableProperty(event.getAsJsonObject("geo_ip"), "latitude", latitude, JsonElement::getAsDouble);
                checkNullableProperty(event.getAsJsonObject("geo_ip"), "longitude", longitude, JsonElement::getAsDouble);
                checkNullableProperty(event.getAsJsonObject("geo_ip"), "country_code2", countryCode2, JsonElement::getAsString);
            } else if (event.getAsJsonObject("geo_ip") != null && !event.getAsJsonObject("geo_ip").isJsonNull()) {
                setFailedPropertyName("geo_ip");
                setExpectedValue(null);
                setGotValue("not null");
                return false;
            }

            checkNullableStringArrayProperty(event, "threat_type", threatType);
            checkNullableStringArrayProperty(event, "identifier", identifier);
        } catch (MatchException e) {
            return false;
        }
        return true;
    }

    public static EventMatcher event(final String timestamp,
                                     final Integer accuracy,
                                     final Integer resolverId,
                                     final String action,
                                     final String reason,
                                     final String clientIp,
                                     final String domain,
                                     final String deviceId,
                                     final String[] threatType,
                                     final String[] identifier,
                                     final Double latitude,
                                     final Double longitude,
                                     final String countryCode2) {
        return new EventMatcher(timestamp, accuracy, resolverId, action, reason, clientIp, domain, deviceId, threatType,
                identifier, latitude, longitude, countryCode2);
    }
}
