package com.ai.FlatServer.domain.dto.request.file;

import lombok.Data;

@Data
public class FilePatchRequest {
    private Long newFolderId;
    private Integer iconId;
    private Boolean isFav;
}
