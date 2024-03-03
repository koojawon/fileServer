package com.ai.FlatServer.domain.mapper;

import com.ai.FlatServer.domain.dto.message.JsonMessageBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.kurento.client.IceCandidate;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.stereotype.Component;

@Component
public class JsonMessageEncoder {

    private Gson gson;

    public String toTargetInfoMessage(String targetId) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "targetInfo");
        response.addProperty("targetId", targetId);
        return gson.toJson(response);
    }

    public String toRejectMessage() {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "rejected");
        response.addProperty("message", "No such sender.");
        return gson.toJson(response);
    }

    public String toViewerResponse(String viewerSdpAnswer) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", viewerSdpAnswer);
        return gson.toJson(response);
    }

    public JsonObject toPresenterResponse(String presenterSdpAnswer) {
        return new JsonMessageBuilder()
                .addProperty("id", "presenterResponse")
                .addProperty("response", "accepted")
                .addProperty("sdpAnswer", presenterSdpAnswer)
                .buildAsJsonObj();
    }

    public String toIceCandidateMessage(IceCandidate iceCandidate) {
        return gson.toJson(toIceCandidateJsonObject(iceCandidate));
    }

    public JsonObject toIceCandidateJsonObject(IceCandidate iceCandidate) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.add("candidate", JsonUtils.toJsonObject(iceCandidate));
        return response;
    }

    public String toEndMessage(String id) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "stopCommunication");
        response.addProperty("targetId", id);
        return gson.toJson(response);
    }
}
