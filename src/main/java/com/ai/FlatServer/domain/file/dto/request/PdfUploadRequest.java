package com.ai.FlatServer.domain.file.dto.request;

import lombok.Data;

@Data
public class PdfUploadRequest {
    private Long folderId;
    private Integer iconId;
}