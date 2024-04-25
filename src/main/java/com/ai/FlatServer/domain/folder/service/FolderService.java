package com.ai.FlatServer.domain.folder.service;

import com.ai.FlatServer.domain.file.respository.FileInfoRepository;
import com.ai.FlatServer.domain.file.respository.dao.FileInfo;
import com.ai.FlatServer.domain.folder.dto.mapper.FolderMapper;
import com.ai.FlatServer.domain.folder.dto.request.FolderPatchRequest;
import com.ai.FlatServer.domain.folder.dto.response.FolderInfo;
import com.ai.FlatServer.domain.folder.enums.FolderType;
import com.ai.FlatServer.domain.folder.repository.FolderRepository;
import com.ai.FlatServer.domain.folder.repository.entity.Folder;
import com.ai.FlatServer.domain.user.enums.Role;
import com.ai.FlatServer.domain.user.repository.entity.User;
import com.ai.FlatServer.global.exceptions.FlatErrorCode;
import com.ai.FlatServer.global.exceptions.FlatException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileInfoRepository fileInfoRepository;

    private final CacheManager cacheManager;

    @PostConstruct
    @Transactional
    public void init() {
        try {
            folderRepository.findById(1L).orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
        } catch (FlatException e) {
            Folder folder = Folder.builder().parentId(null).type(FolderType.ROOT).folderName("root").build();
            folderRepository.save(folder);
        }
    }

    @Cacheable(cacheNames = "folderCache", key = "'folderInfoCache' + #targetFolderId")
    public FolderInfo getFolderWithId(Long targetFolderId) {
        Folder folder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));

        List<FileInfo> subFiles = fileInfoRepository.findAllByParentFolderId(folder.getId());
        List<Folder> subFolders = folderRepository.findByParentId(folder.getId());
        return FolderMapper.FolderToFolderInfoMapper(folder, subFiles, subFolders);
    }

    @Transactional
    @CacheEvict(cacheNames = "folderCache", key = "'folderInfoCache' + #currentFolderId")
    public void createFolder(String folderName, Long currentFolderId, User user) {
        Folder surFolder = folderRepository.findById(currentFolderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
        Folder folder = Folder.builder()
                .folderName(folderName)
                .parentId(surFolder.getId())
                .type(FolderType.LEAF)
                .ownerId(user.getId())
                .build();
        folderRepository.save(folder);
    }

    @Transactional
    public void createRootFolderFor(User user) {
        Folder folder = Folder.builder()
                .folderName("root")
                .ownerId(user.getId())
                .parentId(1L)
                .type(FolderType.ROOT)
                .build();
        folderRepository.saveAndFlush(folder);
        user.setUserRootFolderId(folder.getId());
    }

    @Transactional
    public void deleteFolder(Long targetFolderId) {
        Folder folder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));

        List<Long> folderIds = searchSubFolderIds(folder);

        fileInfoRepository.deleteAllByParentFolderId(folderIds);
        folderRepository.deleteAllByIds(folderIds);

        Objects.requireNonNull(cacheManager.getCache("folderCache"))
                .evict("folderInfoCache" + folder.getParentId());
        for (Long id : folderIds) {
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict("folderInfoCache" + id);
        }
    }

    private List<Long> searchSubFolderIds(Folder folder) {
        List<Long> folders = new ArrayList<>();
        Queue<Folder> q = new LinkedList<>(folderRepository.findByParentId(folder.getId()));
        while (!q.isEmpty()) {
            Folder cur = q.poll();
            folders.add(cur.getId());
            for (Folder f : folderRepository.findByParentId(cur.getId())) {
                q.offer(f);
            }
        }
        folders.add(folder.getId());
        return folders;
    }

    @Transactional
    @CacheEvict(cacheNames = "folderCache", key = "#targetFolderId")
    public void patchUpdate(FolderPatchRequest folderPatchRequest, Long targetFolderId) {
        Folder folder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
        Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(folder.getParentId());
        if (folderPatchRequest.getNewName() != null) {
            folder.setFolderName(folderPatchRequest.getNewName());
        }
        if (folderPatchRequest.getNewParent() != null) {
            Folder newParent = folderRepository.findById(folderPatchRequest.getNewParent())
                    .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(newParent.getId());
            folder.setParentId(newParent.getId());
        }
    }


    public void checkFolderAuthority(User user, Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
        if (folder.getOwnerId() == null || (!user.getId().equals(folder.getOwnerId()) && !user.getRole()
                .equals(Role.ADMIN))) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
    }
}
