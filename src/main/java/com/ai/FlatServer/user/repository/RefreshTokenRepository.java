package com.ai.FlatServer.user.repository;

import com.ai.FlatServer.user.repository.entity.UserRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<UserRefreshToken, String> {
}
