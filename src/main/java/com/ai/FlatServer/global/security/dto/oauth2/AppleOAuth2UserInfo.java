package com.ai.FlatServer.global.security.dto.oauth2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson((String) attributes.get("user"), JsonObject.class);
        return jsonObject.get("email").getAsString();
    }

    @Override
    public String getNickname() {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson((String) attributes.get("user"), JsonObject.class);
        JsonObject nameObject = jsonObject.getAsJsonObject("name");
        return nameObject.get("firstName").getAsString() + nameObject.get("lastName").getAsString();
    }

    @Override
    public String getEmail() {
        return "";
    }
}
