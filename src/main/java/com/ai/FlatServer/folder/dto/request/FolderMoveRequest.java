package com.ai.FlatServer.folder.dto.request;

import lombok.Data;

@Data
public class FolderMoveRequest {
    private Long to;
    private Long currentFolderId;
}
