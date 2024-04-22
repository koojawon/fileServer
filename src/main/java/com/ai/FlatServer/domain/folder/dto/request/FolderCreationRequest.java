package com.ai.FlatServer.domain.folder.dto.request;

import lombok.Data;

@Data
public class FolderCreationRequest {
    private String folderName;
    private Long currentFolderId;
}
