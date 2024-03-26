package com.ai.FlatServer.folder.dto.response;

import com.ai.FlatServer.file.dto.response.FileNameInfo;
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
