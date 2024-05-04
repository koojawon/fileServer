package com.ai.FlatServer.domain.file.dto.request;

import lombok.Data;

@Data
public class PdfUploadRequest {
    private final Long folderId;
    private final Integer iconId;
}
