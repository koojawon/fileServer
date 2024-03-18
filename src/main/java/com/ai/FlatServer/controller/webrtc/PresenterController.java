package com.ai.FlatServer.controller.webrtc;

import com.ai.FlatServer.domain.dto.message.JsonMessageBuilder;
import com.ai.FlatServer.service.webrtc.PresenterService;
import com.ai.FlatServer.service.webrtc.ViewerService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenterController extends TextWebSocketHandler {
    private PresenterService presenterService;
    private ViewerService viewerService;
    private Gson gson;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        switch (jsonMessage.get("id").getAsString()) {
            case "presenter":
                handlePresenterMessage(session, jsonMessage);
                break;
            case "onIceCandidate": {
                handleIceMessage(jsonMessage, session);
                break;
            }
            case "stop":
                handleStopMessage(session);
                break;
            default:
                break;
        }
    }

    private void handlePresenterMessage(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        try {
            presenterService.presenter(session, jsonMessage);
            viewerService.sendTargetInfo(session.getId(), jsonMessage);
        } catch (Exception e) {
            handleErrorResponse(e, session, "presenterResponse");
        }
    }

    private synchronized void handleStopMessage(WebSocketSession session) {
        try {
            presenterService.stop(session);
            viewerService.notifyEnd(session);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    private void handleIceMessage(final JsonObject jsonMessage, final WebSocketSession session) {
        presenterService.iceCandidateReceived(jsonMessage, session);
    }

    private void handleErrorResponse(Throwable throwable, WebSocketSession session, String responseId)
            throws IOException {
        log.error(throwable.getMessage(), throwable);

        session.sendMessage(new TextMessage(
                new JsonMessageBuilder()
                        .addProperty("id", responseId)
                        .addProperty("response", "rejected")
                        .addProperty("message", throwable.getMessage())
                        .buildAsString()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        handleStopMessage(session);
    }
}
