package com.ai.FlatServer.folder.dto.request;

import lombok.Data;

@Data
public class FolderPatchRequest {
    private String newName;
    private Long newParent;
}
