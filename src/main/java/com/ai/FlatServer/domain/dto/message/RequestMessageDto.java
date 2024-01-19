package com.ai.FlatServer.domain.dto.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestMessageDto {
    private String fileUid;
}
