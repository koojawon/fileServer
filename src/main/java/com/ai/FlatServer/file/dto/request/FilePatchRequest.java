package com.ai.FlatServer.file.dto.request;

import lombok.Data;

@Data
public class FilePatchRequest {
    private Long newFolderId;
    private Integer iconId;
    private Boolean isFav;
}
