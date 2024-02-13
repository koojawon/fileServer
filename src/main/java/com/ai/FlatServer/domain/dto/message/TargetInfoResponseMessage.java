package com.ai.FlatServer.domain.dto.message;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor

public class TargetInfoResponseMessage {
    private final String id;
    private final String targetId;
    private final String sdpOffer;
}
