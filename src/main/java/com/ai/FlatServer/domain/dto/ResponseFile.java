package com.ai.FlatServer.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseFile {
    private final String uid;

    private final String fileName;
}
