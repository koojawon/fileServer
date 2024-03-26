package com.ai.FlatServer.folder.service;

import com.ai.FlatServer.folder.dto.response.FolderInfo;
import com.ai.FlatServer.folder.dto.request.FolderPatchRequest;
import com.ai.FlatServer.folder.dto.mapper.FolderMapper;
import com.ai.FlatServer.file.respository.FileInfoRepository;
import com.ai.FlatServer.folder.repository.FolderRepository;
import com.ai.FlatServer.folder.repository.entity.Folder;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileInfoRepository fileInfoRepository;

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

    public FolderInfo getFolderWithId(Long folderId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(NoSuchElementException::new);
        return FolderMapper.FolderToFolderInfoMapper(folder);
    }

    @Transactional
    public void createFolderAt(String folderName, Long currentFolderId) {
        Folder surFolder = folderRepository.findById(currentFolderId).orElseThrow(NoSuchElementException::new);
        Folder folder = Folder.builder().folderName(folderName).parent(surFolder).build();
        surFolder.getSubDirs().add(folder);
        folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(Long currentFolderId) {
        Folder folder = folderRepository.findById(currentFolderId).orElseThrow(NoSuchElementException::new);
        fileInfoRepository.deleteAll(folder.getSubFiles());
        folderRepository.delete(folder);
    }

    @Transactional
    public void patchUpdate(FolderPatchRequest folderPatchRequest, Long currentFolderId) {
        Folder folder = folderRepository.findById(currentFolderId).orElseThrow(NoSuchElementException::new);
        if (folderPatchRequest.getNewName() != null) {
            folder.setFolderName(folderPatchRequest.getNewName());
        }
        if (folderPatchRequest.getNewParent() != null) {
            folder.getParent().getSubDirs().remove(folder);
            Folder newParent = folderRepository.findById(folderPatchRequest.getNewParent())
                    .orElseThrow(NoSuchElementException::new);
            folder.setParent(newParent);
            newParent.getSubDirs().add(folder);
        }
    }
}
