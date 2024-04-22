package com.ai.FlatServer.global.security.dto.oauth2;

import java.util.Map;

public class XOAuth2UserInfo extends OAuth2UserInfo {

    public XOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getNickname() {
        return "";
    }
}
