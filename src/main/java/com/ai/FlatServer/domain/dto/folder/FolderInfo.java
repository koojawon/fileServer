package com.ai.FlatServer.domain.dto.folder;

import com.ai.FlatServer.domain.dto.file.FileNameInfo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class FolderInfo {
    private final List<FolderInfo> subDirs = new ArrayList<>();
    private final List<FileNameInfo> subFiles = new ArrayList<>();
    private Long id;
    private String folderName;
    private Long parentId;
    private LocalDateTime modDate;
}
