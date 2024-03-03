package com.ai.FlatServer.domain.dto.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonMessageBuilder {

    private final JsonObject jsonObject = new JsonObject();

    public JsonMessageBuilder addProperty(String property, String value) {
        jsonObject.addProperty(property, value);
        return this;
    }

    public JsonMessageBuilder add(String property, JsonElement jsonElement) {
        jsonObject.add(property, jsonElement);
        return this;
    }

    public String buildAsString() {
        return this.jsonObject.toString();
    }

    public JsonObject buildAsJsonObj() {
        return this.jsonObject;
    }
}
