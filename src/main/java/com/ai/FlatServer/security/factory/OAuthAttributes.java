package com.ai.FlatServer.security.factory;

import com.ai.FlatServer.security.dto.oauth2.AppleOAuth2UserInfo;
import com.ai.FlatServer.security.dto.oauth2.FacebookOAuth2UserInfo;
import com.ai.FlatServer.security.dto.oauth2.GoogleOAuth2UserInfo;
import com.ai.FlatServer.security.dto.oauth2.OAuth2UserInfo;
import com.ai.FlatServer.security.dto.oauth2.XOAuth2UserInfo;
import com.ai.FlatServer.user.enums.Role;
import com.ai.FlatServer.user.enums.SocialType;
import com.ai.FlatServer.user.repository.entity.User;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OAuthAttributes {

    private String nameAttributeKey;

    private OAuth2UserInfo oAuth2UserInfo;

    public static OAuthAttributes of(SocialType socialType, String userNameAttributeName,
                                     Map<String, Object> attributes) {
        switch (socialType) {
            case GOOGLE -> {
                return ofGoogle(userNameAttributeName, attributes);
            }
            case FACEBOOK -> {
                return ofFacebook(userNameAttributeName, attributes);
            }
            case X -> {
                return ofX(userNameAttributeName, attributes);
            }
            case APPLE -> {
                return ofApple(userNameAttributeName, attributes);
            }
            default -> {
                return null;
            }
        }
    }

    private static OAuthAttributes ofX(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuth2UserInfo(new XOAuth2UserInfo(attributes))
                .build();
    }

    private static OAuthAttributes ofApple(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuth2UserInfo(new AppleOAuth2UserInfo(attributes))
                .build();
    }

    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();
    }

    public static OAuthAttributes ofFacebook(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuth2UserInfo(new FacebookOAuth2UserInfo(attributes))
                .build();
    }

    public User toEntity(SocialType socialType, OAuth2UserInfo oAuth2UserInfo) {
        return User.builder()
                .socialType(socialType)
                .socialId(oAuth2UserInfo.getId())
                .email(oAuth2UserInfo.getId() + "-" + socialType.name() + "@socialUser.com")
                .nickname(oAuth2UserInfo.getNickname())
                .role(Role.USER)
                .folderCount(5).build();
    }
}
