package com.ai.FlatServer.rabbitmq.mapper;

import com.ai.FlatServer.webrtc.message.JsonMessageBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.kurento.client.IceCandidate;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.stereotype.Component;

@Component
public class JsonMessageEncoder {

    private Gson gson;

    public JsonObject toTargetInfoMessage(String targetId, String fileName) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "targetInfo");
        response.addProperty("targetId", targetId);
        response.addProperty("fileName", fileName);
        return response;
    }

    public JsonObject toRejectMessage() {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "rejected");
        response.addProperty("message", "No such sender.");
        return response;
    }

    public JsonObject toViewerResponse(String viewerSdpAnswer) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", viewerSdpAnswer);
        return response;
    }

    public JsonObject toPresenterResponse(String presenterSdpAnswer) {
        return new JsonMessageBuilder()
                .addProperty("id", "presenterResponse")
                .addProperty("response", "accepted")
                .addProperty("sdpAnswer", presenterSdpAnswer)
                .buildAsJsonObj();
    }

    public JsonObject toIceCandidateMessage(IceCandidate iceCandidate) {
        return toIceCandidateJsonObject(iceCandidate);
    }

    public JsonObject toIceCandidateJsonObject(IceCandidate iceCandidate) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.add("candidate", JsonUtils.toJsonObject(iceCandidate));
        return response;
    }

    public JsonObject toEndMessage(String id) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "stopCommunication");
        response.addProperty("targetId", id);
        return response;
    }
}
