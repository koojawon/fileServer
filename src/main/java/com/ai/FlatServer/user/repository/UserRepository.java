package com.ai.FlatServer.user.repository;

import com.ai.FlatServer.user.enums.SocialType;
import com.ai.FlatServer.user.repository.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    boolean existsByEmail(String email);
}
