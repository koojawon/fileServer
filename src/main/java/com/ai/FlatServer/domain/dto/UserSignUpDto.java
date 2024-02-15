package com.ai.FlatServer.domain.dto;

import lombok.Data;

@Data
public class UserSignUpDto {

    String email;
    String nickname;
    String password;
}
