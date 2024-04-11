package com.ai.FlatServer.folder.service;

import com.ai.FlatServer.exceptions.FlatErrorCode;
import com.ai.FlatServer.exceptions.FlatException;
import com.ai.FlatServer.file.respository.FileInfoRepository;
import com.ai.FlatServer.file.respository.dao.FileInfo;
import com.ai.FlatServer.folder.dto.mapper.FolderMapper;
import com.ai.FlatServer.folder.dto.request.FolderPatchRequest;
import com.ai.FlatServer.folder.dto.response.FolderInfo;
import com.ai.FlatServer.folder.enums.FolderType;
import com.ai.FlatServer.folder.repository.FolderRepository;
import com.ai.FlatServer.folder.repository.entity.Folder;
import com.ai.FlatServer.user.enums.Role;
import com.ai.FlatServer.user.repository.entity.User;
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
            Folder folder = Folder.builder().parent(null).folderName("root").build();
            folderRepository.save(folder);
        }
    }

    @Cacheable(value = "folderCache", key = "'folderInfoCache' + #folderId")
    public FolderInfo getFolderWithId(Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
        
        List<FileInfo> folderInfos = fileInfoRepository.findAllByParentFolderId(folder.getId());
        return FolderMapper.FolderToFolderInfoMapper(folder, folderInfos);
    }

    @Transactional
    @CacheEvict(value = "folderCache", key = "'folderInfoCache' + #currentFolderId")
    public void createFolder(String folderName, Long currentFolderId, User user) {
        Folder surFolder = folderRepository.findById(currentFolderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
        Folder folder = Folder.builder()
                .folderName(folderName)
                .parent(surFolder)
                .type(FolderType.LEAF)
                .owner(user)
                .build();
        surFolder.getSubDirs().add(folder);
        folderRepository.save(folder);
    }

    @Transactional
    public void createRootFolderFor(User user) {
        Folder folder = Folder.builder().folderName("root").type(FolderType.ROOT).build();
        user.setUserRootFolder(folder);
        folderRepository.save(folder);
    }

    @Transactional
    @CacheEvict(value = "fileCache", key = "'all'")
    public void deleteFolder(Long targetFolderId) {
        Folder folder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));

        List<Long> folderIds = searchSubFolderIds(folder);
        // 파일 삭제 가능할까?

        fileInfoRepository.deleteAllByParentFolderId(folderIds);
        folderRepository.deleteAllByIds(folderIds);

        Objects.requireNonNull(cacheManager.getCache("folderCache"))
                .evict("folderInfoCache" + folder.getParent().getId());
        for (Long id : folderIds) {
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict("folderInfoCache" + id);
        }
    }

    private List<Long> searchSubFolderIds(Folder folder) {
        List<Long> files = new ArrayList<>();
        Queue<Folder> q = new LinkedList<>(folder.getSubDirs());
        while (!q.isEmpty()) {
            Folder cur = q.poll();
            files.add(cur.getId());
            for (Folder f : cur.getSubDirs()) {
                q.offer(f);
            }
        }
        files.add(folder.getId());
        return files;
    }

    @Transactional
    @CacheEvict(value = "folderCache", key = "#targetFolderId")
    public void patchUpdate(FolderPatchRequest folderPatchRequest, Long targetFolderId) {
        Folder folder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
        Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(folder.getParent().getId());
        if (folderPatchRequest.getNewName() != null) {
            folder.setFolderName(folderPatchRequest.getNewName());
        }
        if (folderPatchRequest.getNewParent() != null) {
            folder.getParent().getSubDirs().remove(folder);
            Folder newParent = folderRepository.findById(folderPatchRequest.getNewParent())
                    .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(newParent.getId());
            folder.setParent(newParent);
            newParent.getSubDirs().add(folder);
        }
    }


    public void checkFolderAuthority(User user, Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FlatException(FlatErrorCode.NO_SUCH_FOLDER_ID));
        if (folder.getOwner() == null || (!user.equals(folder.getOwner()) && !user.getRole().equals(Role.ADMIN))) {
            throw new FlatException(FlatErrorCode.NO_AUTHORITY);
        }
    }
}
