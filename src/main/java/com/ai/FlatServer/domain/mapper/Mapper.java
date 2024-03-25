package com.ai.FlatServer.domain.mapper;

import com.ai.FlatServer.domain.dto.file.FileNameInfo;
import com.ai.FlatServer.domain.dto.folder.FolderInfo;
import com.ai.FlatServer.repository.entity.FileInfo;
import com.ai.FlatServer.repository.entity.Folder;
import org.springframework.stereotype.Component;

@Component
public class Mapper {
    public static FileNameInfo FileInfoToFileNameInfoMapper(FileInfo fileInfo) {
        return FileNameInfo.builder()
                .id(fileInfo.getId())
                .iconId(fileInfo.getIconId())
                .modDate(fileInfo.getModDate())
                .name(fileInfo.getOriginalFileName())
                .build();
    }

    private static FolderInfo FolderToFolderInfo(Folder folder) {
        if (folder != null) {
            FolderInfo folderInfo = FolderInfo.builder()
                    .id(folder.getId())
                    .modDate(folder.getModDate())
                    .parentId(null)
                    .folderName(folder.getFolderName())
                    .build();
            if (folder.getParent() != null) {
                folderInfo.setParentId(folder.getParent().getId());
            }
            return folderInfo;
        }
        return null;
    }

    public static FolderInfo FolderToFolderInfoMapper(Folder folder) {
        FolderInfo folderInfo = FolderToFolderInfo(folder);
        for (Folder f : folder.getSubDirs()) {
            folderInfo.getSubDirs().add(FolderToFolderInfo(f));
        }
        for (FileInfo f : folder.getSubFiles()) {
            folderInfo.getSubFiles().add(FileInfoToFileNameInfoMapper(f));
        }
        return folderInfo;
    }
}
