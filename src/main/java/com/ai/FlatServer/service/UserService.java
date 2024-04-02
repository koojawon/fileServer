package com.ai.FlatServer.service;

import com.ai.FlatServer.domain.dao.User;
import com.ai.FlatServer.domain.dto.UserSignUpDto;
import com.ai.FlatServer.enums.Role;
import com.ai.FlatServer.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signUp(UserSignUpDto userSignUpDto) throws Exception {
        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일");
        }
        if (userRepository.findByNickname(userSignUpDto.getNickname()).isPresent()) {
            throw new Exception("이미 존재하는 닉네임");
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .nickname(userSignUpDto.getNickname())
                .password(userSignUpDto.getPassword())
                .role(Role.USER)
                .build();

        user.passwordEncode(passwordEncoder);
        userRepository.save(user);
    }
}
