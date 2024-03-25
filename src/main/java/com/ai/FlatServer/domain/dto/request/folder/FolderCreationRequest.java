package com.ai.FlatServer.domain.dto.request.folder;

import lombok.Data;

@Data
public class FolderCreationRequest {
    private String folderName;
    private Long currentFolderId;
}
