package com.ai.FlatServer.global.security.dto.oauth2;

import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class OAuth2UserInfo {
    protected final Map<String, Object> attributes;

    public abstract String getId();

    public abstract String getNickname();

    public abstract String getEmail();
}
