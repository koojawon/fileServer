package com.ai.FlatServer.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class IceCandidateMessage {
    private String id;
    private String uuid;
    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;
}
