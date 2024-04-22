package com.ai.FlatServer.domain.user.repository;

import com.ai.FlatServer.domain.user.enums.SocialType;
import com.ai.FlatServer.domain.user.repository.entity.User;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    @Cacheable(cacheNames = "userCache", key = "'userInfo'+#email")
    Optional<User> findByEmail(String email);

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    boolean existsByEmail(String email);
}
