package io.whalebone.publicapi.tests.matchers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.whalebone.publicapi.tests.MatchException;
import org.apache.xerces.impl.xpath.regex.Match;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamValidationErrorMatcher extends JsonElementMatcher {

    private String parameter;
    private String value;
    private int errorCode;
    private String errorType;
    private String messageRegexp;
    private String acceptedValues[];

    public ParamValidationErrorMatcher(String parameter, String value, int errorCode, String errorType, String messageRegexp, String[] acceptedValues) {
        this.parameter = parameter;
        this.value = value;
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.messageRegexp = messageRegexp;
        this.acceptedValues = acceptedValues;
    }

    @Override
    protected boolean matchesSafely(JsonElement errorJson) {
        JsonObject error = errorJson.getAsJsonObject();
        try {
            checkNullableProperty(error, "parameter", parameter, JsonElement::getAsString);
            checkNullableProperty(error, "value", value, JsonElement::getAsString);
            checkNullableProperty(error, "error_code", errorCode, JsonElement::getAsInt);
            checkNullableProperty(error, "error", errorType, JsonElement::getAsString);
            checkNullableStringArrayProperty(error, "accepted_values", acceptedValues);
        } catch (MatchException me) {
            return false;
        }
        if (messageRegexp != null) {
            Pattern pattern = Pattern.compile(messageRegexp);
            Matcher matcher = pattern.matcher(error.get("message").getAsString());
            if (!matcher.matches()) {
                setFailedPropertyName("message");
                setExpectedValue("matches " + messageRegexp);
                setGotValue(error.get("message").getAsString());
                return false;
            }
        }
        return true;
    }
}
