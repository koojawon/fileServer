package com.ai.FlatServer.global.security.service;

import com.ai.FlatServer.domain.user.enums.SocialType;
import com.ai.FlatServer.domain.user.repository.UserRepository;
import com.ai.FlatServer.domain.user.repository.entity.User;
import com.ai.FlatServer.global.security.dto.oauth2.CustomOAuth2User;
import com.ai.FlatServer.global.security.factory.OAuthAttributes;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String APPLE = "apple";
    private static final String FACEBOOK = "facebook";
    private static final String X = "x";
    private static final String GOOGLE = "google";
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        SocialType socialType = getSocialType(registrationId);
        if (socialType == null) {
            throw new OAuth2AuthenticationException("Wrong Social URI");
        }

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String userNameAttributesName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuthAttributes oAuthAttributes = OAuthAttributes.of(socialType, userNameAttributesName, attributes);
        User createdUser = getUser(Objects.requireNonNull(oAuthAttributes), socialType);
        return new CustomOAuth2User(Collections.singleton(new SimpleGrantedAuthority(createdUser.getRole().getKey())),
                attributes, oAuthAttributes.getNameAttributeKey(), createdUser.getEmail(), createdUser.getRole());
    }

    private SocialType getSocialType(String registrationId) {
        switch (registrationId) {
            case X -> {
                return SocialType.X;
            }
            case FACEBOOK -> {
                return SocialType.FACEBOOK;
            }
            case APPLE -> {
                return SocialType.APPLE;
            }
            case GOOGLE -> {
                return SocialType.GOOGLE;
            }
            default -> {
                return null;
            }
        }
    }

    private User getUser(OAuthAttributes attributes, SocialType socialType) {
        User findUser = userRepository.findBySocialTypeAndSocialId(socialType, attributes.getOAuth2UserInfo().getId())
                .orElse(null);
        if (findUser == null) {
            return saveUser(attributes, socialType);
        }
        return findUser;
    }

    @Transactional
    private User saveUser(OAuthAttributes attributes, SocialType socialType) {
        User user = attributes.toEntity(socialType, attributes.getOAuth2UserInfo());
        return userRepository.save(user);
    }
}
