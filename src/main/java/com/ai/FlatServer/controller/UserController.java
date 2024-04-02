package com.ai.FlatServer.controller;

import com.ai.FlatServer.domain.dto.UserSignUpDto;
import com.ai.FlatServer.service.UserService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> signUp(@RequestBody UserSignUpDto userSignUpDto) {
        try {
            userService.signUp(userSignUpDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.created(URI.create("/")).build();
    }
}
