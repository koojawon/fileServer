package com.ai.FlatServer.user.service;

import com.ai.FlatServer.folder.service.FolderService;
import com.ai.FlatServer.user.dto.UserEmailDupCheckDto;
import com.ai.FlatServer.user.dto.UserSignUpDto;
import com.ai.FlatServer.user.enums.Role;
import com.ai.FlatServer.user.repository.UserRepository;
import com.ai.FlatServer.user.repository.entity.User;
import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FolderService folderService;

    public void signUp(UserSignUpDto userSignUpDto) throws Exception {
        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일");
        }
        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .nickname(userSignUpDto.getNickname())
                .password(userSignUpDto.getPassword())
                .role(Role.USER)
                .build();

        user.passwordEncode(passwordEncoder);
        folderService.createRootFolderFor(user);
        userRepository.save(user);
    }

    public boolean checkEmailDup(UserEmailDupCheckDto userEmailDupCheckDto) {
        return userRepository.existsByEmail(userEmailDupCheckDto.getEmail());
    }

    public User getCurrentUser() {
        SecurityContext sc = SecurityContextHolder.getContext();
        UserDetails principal = (UserDetails) sc.getAuthentication().getPrincipal();
        return userRepository.findByEmail(principal.getUsername()).orElseThrow(NoSuchElementException::new);
    }

    public boolean checkCreateAvailability(User user) {
        return user.getFolderCount() > 0;
    }

    public void decreaseFolderCount(User user) {
        user.setFolderCount(user.getFolderCount() - 1);
    }

    public void increaseFolderCount(User currentUser) {
        currentUser.setFolderCount(currentUser.getFolderCount() + 1);
    }
}
