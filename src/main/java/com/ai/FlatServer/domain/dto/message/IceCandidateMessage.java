package com.ai.FlatServer.domain.dto.message;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class IceCandidateMessage {
    private String id;
    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;
}
