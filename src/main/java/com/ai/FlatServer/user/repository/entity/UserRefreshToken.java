package com.ai.FlatServer.user.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@AllArgsConstructor
@Data
@RedisHash("userRefreshToken")
public class UserRefreshToken {

    @Id
    private String email;

    private String refreshToken;
}
