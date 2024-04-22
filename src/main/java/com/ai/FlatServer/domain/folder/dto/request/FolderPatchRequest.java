package com.ai.FlatServer.domain.folder.dto.request;

import lombok.Data;

@Data
public class FolderPatchRequest {
    private String newName;
    private Long newParent;
}
