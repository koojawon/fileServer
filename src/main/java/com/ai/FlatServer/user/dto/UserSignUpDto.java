package com.ai.FlatServer.user.dto;

import lombok.Data;

@Data
public class UserSignUpDto {

    String email;
    String nickname;
    String password;
}
