package com.ai.FlatServer.domain.dto.request.folder;

import lombok.Data;

@Data
public class FolderMoveRequest {
    private Long to;
    private Long currentFolderId;
}
