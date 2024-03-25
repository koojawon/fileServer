package com.ai.FlatServer.domain.dto.request.folder;

import lombok.Data;

@Data
public class FolderPatchRequest {
    private String newName;
    private Long newParent;
}
