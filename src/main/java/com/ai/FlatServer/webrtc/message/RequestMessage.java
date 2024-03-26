package com.ai.FlatServer.webrtc.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestMessage {
    private final String id = "File";
    private String fileUid;
}
