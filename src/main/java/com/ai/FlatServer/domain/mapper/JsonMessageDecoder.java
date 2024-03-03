package com.ai.FlatServer.domain.mapper;

import com.ai.FlatServer.domain.dto.message.IceCandidateMessage;
import com.ai.FlatServer.domain.dto.message.TargetInfoResponseMessage;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;

@Component
public class JsonMessageDecoder {

    public TargetInfoResponseMessage toTargetInfoResponseMessage(JsonObject jsonObject) {
        return TargetInfoResponseMessage.builder()
                .id(jsonObject.get("id").getAsString())
                .uuid(jsonObject.get("uuid").getAsString())
                .targetId(jsonObject.get("targetId").getAsString())
                .sdpOffer(jsonObject.get("sdpOffer").getAsString())
                .build();
    }

    public IceCandidateMessage toIceCandidateMessage(JsonObject jsonObject) {
        return IceCandidateMessage.builder()
                .id(jsonObject.get("id").getAsString())
                .uuid(jsonObject.get("uuid").getAsString())
                .candidate(jsonObject.get("candidate").getAsString())
                .sdpMid(jsonObject.get("sdpMid").getAsString())
                .sdpMLineIndex(jsonObject.get("sdpMLineIndex").getAsInt())
                .build();
    }
}
