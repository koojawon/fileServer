package com.ai.FlatServer.domain.user.controller;

import com.ai.FlatServer.domain.folder.service.FolderService;
import com.ai.FlatServer.domain.user.dto.UserEmailDupCheckDto;
import com.ai.FlatServer.domain.user.dto.UserSignUpDto;
import com.ai.FlatServer.domain.user.repository.entity.User;
import com.ai.FlatServer.domain.user.service.UserService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<String> signUp(@RequestBody UserSignUpDto userSignUpDto) {
        User user = userService.signUp(userSignUpDto);
        folderService.createRootFolderFor(user);
        return ResponseEntity.created(URI.create("")).build();
    }

    @PostMapping("/emailCheck")
    public ResponseEntity<Boolean> checkEmailDup(@RequestBody UserEmailDupCheckDto userEmailDupCheckDto) {
        userService.checkEmailDup(userEmailDupCheckDto);
        return ResponseEntity.ok().build();
    }
}
