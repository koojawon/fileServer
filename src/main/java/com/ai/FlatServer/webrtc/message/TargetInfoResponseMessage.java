package com.ai.FlatServer.webrtc.message;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor

public class TargetInfoResponseMessage {
    private final String id;
    private final String targetId;
    private final String uuid;
    private final String sdpOffer;
}
