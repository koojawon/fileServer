package com.ai.FlatServer.global.security.service;

import com.ai.FlatServer.domain.file.respository.FileInfoRepository;
import com.ai.FlatServer.domain.file.respository.dao.FileInfo;
import com.ai.FlatServer.domain.folder.repository.FolderRepository;
import com.ai.FlatServer.domain.folder.repository.entity.Folder;
import com.ai.FlatServer.domain.user.repository.entity.User;
import com.ai.FlatServer.global.exceptions.FlatErrorCode;
import com.ai.FlatServer.global.exceptions.FlatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authChecker")
@RequiredArgsConstructor
@Slf4j
public class AuthorizationCheckService {

    private final FileInfoRepository fileInfoRepository;
    private final FolderRepository folderRepository;

    public boolean checkFolderAuthority(Long folderId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Folder folder = folderRepository.findById(folderId).orElseThrow();
        log.info("owner ID : {} , user ID : {}", folder.getOwnerId(), user.getId());
        return folder.checkAuthority(user);
    }

    public boolean checkFileAuthority(Long fileId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        FileInfo fileInfo = fileInfoRepository.findById(fileId).orElseThrow();
        return fileInfo.checkAuthority(user);
    }

    private User getUserFromAuthentication(Authentication auth) {
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
}
