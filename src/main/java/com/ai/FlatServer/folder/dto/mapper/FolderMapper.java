package com.ai.FlatServer.folder.dto.mapper;

import com.ai.FlatServer.file.dto.response.FileNameInfo;
import com.ai.FlatServer.file.respository.dao.FileInfo;
import com.ai.FlatServer.folder.dto.response.FolderInfo;
import com.ai.FlatServer.folder.repository.entity.Folder;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FolderMapper {
    public static FileNameInfo FileInfoToFileNameInfoMapper(FileInfo fileInfo) {
        return FileNameInfo.builder()
                .id(fileInfo.getId())
                .iconId(fileInfo.getIconId())
                .modDate(fileInfo.getModDate())
                .isFav(fileInfo.getFav())
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

    public static FolderInfo FolderToFolderInfoMapper(Folder folder, List<FileInfo> subFiles) {
        FolderInfo folderInfo = FolderToFolderInfo(folder);
        for (Folder f : folder.getSubDirs()) {
            folderInfo.getSubDirs().add(FolderToFolderInfo(f));
        }
        for (FileInfo f : subFiles) {
            folderInfo.getSubFiles().add(FileInfoToFileNameInfoMapper(f));
        }
        return folderInfo;
    }
}
