package com.ai.FlatServer.user.controller;

import com.ai.FlatServer.user.dto.UserEmailDupCheckDto;
import com.ai.FlatServer.user.dto.UserSignUpDto;
import com.ai.FlatServer.user.service.UserService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
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
        return ResponseEntity.created(URI.create("")).build();
    }

    @PostMapping("/emailCheck")
    public ResponseEntity<Boolean> checkEmailDup(@RequestBody UserEmailDupCheckDto userEmailDupCheckDto) {
        if (userService.checkEmailDup(userEmailDupCheckDto)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/logout")
    public ResponseEntity<Boolean> logout() {
        return ResponseEntity.ok().build();
    }
}
