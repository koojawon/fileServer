package com.ai.FlatServer.domain.user.dto;

import lombok.Data;

@Data
public class UserSignUpDto {

    String email;
    String nickname;
    String password;
}
