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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
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
                .folderCount(5)
                .build();
        user.authorizeUser();
        user.passwordEncode(passwordEncoder);
        userRepository.save(user);
        return user;
    }

    public void checkEmailDup(UserEmailDupCheckDto userEmailDupCheckDto) {
        if (userRepository.existsByEmail(userEmailDupCheckDto.getEmail())) {
            throw new FlatException(FlatErrorCode.DUPLICATED_EMAIL);
        }
    }

    public User getCurrentUser() {
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        if (!auth.isAuthenticated()) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
        Object credentials = auth.getCredentials();
        User user;
        if (credentials instanceof User) {
            user = (User) credentials;
        } else {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
        return user;
    }

    public void checkCreateAvailability(User user) {
        if (user.getFolderCount() <= 0) {
            throw new FlatException(FlatErrorCode.FOLDER_CREATION_LIMIT);
        }
    }

    @Transactional
    public void decreaseFolderCount() {
        User user = getCurrentUser();
        user.setFolderCount(user.getFolderCount() - 1);
        userRepository.save(user);
    }

    @Transactional
    public void increaseFolderCount() {
        User currentUser = getCurrentUser();
        currentUser.setFolderCount(currentUser.getFolderCount() + 1);
        userRepository.save(currentUser);
    }

    public void checkFileAuthority(FileInfo fileInfo) {
        User user = getCurrentUser();
        if (!fileInfo.getOwnerId().equals(user.getId())) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
    }
}
