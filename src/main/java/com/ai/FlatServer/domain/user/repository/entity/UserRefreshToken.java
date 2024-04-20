package com.ai.FlatServer.domain.user.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@AllArgsConstructor
@Data
@Builder
@RedisHash("userRefreshToken")
public class UserRefreshToken {

    @Id
    private String email;

    private String refreshToken;
}
