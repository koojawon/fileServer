package com.ai.FlatServer.folder.service;

import com.ai.FlatServer.file.respository.FileInfoRepository;
import com.ai.FlatServer.file.respository.dao.FileInfo;
import com.ai.FlatServer.folder.dto.mapper.FolderMapper;
import com.ai.FlatServer.folder.dto.request.FolderPatchRequest;
import com.ai.FlatServer.folder.dto.response.FolderInfo;
import com.ai.FlatServer.folder.repository.FolderRepository;
import com.ai.FlatServer.folder.repository.entity.Folder;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
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
            folderRepository.findById(0L).orElseThrow(NoSuchElementException::new);
        } catch (NoSuchElementException e) {
            Folder folder = Folder.builder().parent(null).folderName("root").build();
            folderRepository.save(folder);
        }
    }

    @Cacheable(value = "folderCache", key = "#folderId")
    public FolderInfo getFolderWithId(Long folderId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(NoSuchElementException::new);
        List<FileInfo> folderInfos = fileInfoRepository.findAllByParentFolderId(folder.getId());
        return FolderMapper.FolderToFolderInfoMapper(folder, folderInfos);
    }

    @Transactional
    @CacheEvict(value = "folderCache", key = "#currentFolderId")
    public void createFolderAt(String folderName, Long currentFolderId) {
        Folder surFolder = folderRepository.findById(currentFolderId).orElseThrow(NoSuchElementException::new);
        Folder folder = Folder.builder().folderName(folderName).parent(surFolder).build();
        surFolder.getSubDirs().add(folder);
        folderRepository.save(folder);
    }

    @Transactional
    @CacheEvict(value = "fileCache", key = "'all'")
    public void deleteFolder(Long targetFolderId) {
        Folder folder = folderRepository.findById(targetFolderId).orElseThrow(NoSuchElementException::new);

        List<Long> folderIds = searchSubFolderIds(folder);
        fileInfoRepository.deleteAllByParentFolderId(folderIds);
        folderRepository.deleteAllByIds(folderIds);

        Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(folder.getParent().getId());
        for (Long id : folderIds) {
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(id);
        }
    }

    private List<Long> searchSubFolderIds(Folder folder) {
        List<Long> rval = new ArrayList<>();
        Queue<Folder> q = new LinkedList<>(folder.getSubDirs());
        while (!q.isEmpty()) {
            Folder cur = q.poll();
            rval.add(cur.getId());
            for (Folder f : cur.getSubDirs()) {
                q.offer(f);
            }
        }
        rval.add(folder.getId());
        return rval;
    }

    @Transactional
    @CacheEvict(value = "folderCache", key = "#targetFolderId")
    public void patchUpdate(FolderPatchRequest folderPatchRequest, Long targetFolderId) {
        Folder folder = folderRepository.findById(targetFolderId).orElseThrow(NoSuchElementException::new);
        Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(folder.getParent().getId());
        if (folderPatchRequest.getNewName() != null) {
            folder.setFolderName(folderPatchRequest.getNewName());
        }
        if (folderPatchRequest.getNewParent() != null) {
            folder.getParent().getSubDirs().remove(folder);
            Folder newParent = folderRepository.findById(folderPatchRequest.getNewParent())
                    .orElseThrow(NoSuchElementException::new);
            Objects.requireNonNull(cacheManager.getCache("folderCache")).evict(newParent.getId());
            folder.setParent(newParent);
            newParent.getSubDirs().add(folder);
        }
    }
}
