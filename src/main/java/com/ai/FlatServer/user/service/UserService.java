package com.ai.FlatServer.user.service;

import com.ai.FlatServer.exceptions.FlatErrorCode;
import com.ai.FlatServer.exceptions.FlatException;
import com.ai.FlatServer.file.respository.dao.FileInfo;
import com.ai.FlatServer.user.dto.UserEmailDupCheckDto;
import com.ai.FlatServer.user.dto.UserSignUpDto;
import com.ai.FlatServer.user.repository.UserRepository;
import com.ai.FlatServer.user.repository.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    public User signUp(UserSignUpDto userSignUpDto) {
        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new FlatException(FlatErrorCode.DUPLICATED_EMAIL);
        }
        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .nickname(userSignUpDto.getNickname())
                .password(userSignUpDto.getPassword())
                .build();
        user.authorizeUser();
        user.passwordEncode(passwordEncoder);
        userRepository.save(user);
        return user;
    }

    public boolean checkEmailDup(UserEmailDupCheckDto userEmailDupCheckDto) {
        return userRepository.existsByEmail(userEmailDupCheckDto.getEmail());
    }

    public User getCurrentUser() {
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        if (!auth.isAuthenticated()) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
        UserDetails principal = (UserDetails) auth.getPrincipal();
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }

    public boolean checkCreateAvailability() {
        User user = getCurrentUser();
        return user.getFolderCount() > 0;
    }

    public void decreaseFolderCount() {
        User user = getCurrentUser();
        user.setFolderCount(user.getFolderCount() - 1);
    }

    public void increaseFolderCount() {
        User currentUser = getCurrentUser();

        currentUser.setFolderCount(currentUser.getFolderCount() + 1);
    }

    public void checkFileAuthority(FileInfo fileInfo) {
        User user = getCurrentUser();
        if (!fileInfo.getOwner().equals(user)) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
    }
}
