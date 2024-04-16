package com.ai.FlatServer.webrtc.controller;

import com.ai.FlatServer.webrtc.message.JsonMessageBuilder;
import com.ai.FlatServer.webrtc.service.PresenterService;
import com.ai.FlatServer.webrtc.service.ViewerService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import lombok.NonNull;
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
    private final PresenterService presenterService;
    private final ViewerService viewerService;
    private final Gson gson = new Gson();

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        log.info(jsonMessage.toString());
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

    private void handlePresenterMessage(@NonNull WebSocketSession session, JsonObject jsonMessage) throws IOException {
        try {
            presenterService.presenter(session, jsonMessage);
            viewerService.sendTargetInfo(session.getId(), jsonMessage);
        } catch (Exception e) {
            handleErrorResponse(e, session);
        }
    }

    private synchronized void handleStopMessage(@NonNull WebSocketSession session) {
        try {
            presenterService.stop(session);
            viewerService.notifyEnd(session);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    private void handleIceMessage(final JsonObject jsonMessage, @NonNull final WebSocketSession session) {
        presenterService.iceCandidateReceived(jsonMessage, session);
    }

    private void handleErrorResponse(Throwable throwable, @NonNull WebSocketSession session)
            throws IOException {
        log.error(throwable.getMessage(), throwable);

        session.sendMessage(new TextMessage(
                new JsonMessageBuilder()
                        .addProperty("id", "presenterResponse")
                        .addProperty("response", "rejected")
                        .addProperty("message", throwable.getMessage())
                        .buildAsString()));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        handleStopMessage(session);
    }
}
