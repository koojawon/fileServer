package com.ai.FlatServer.handler;

import com.ai.FlatServer.domain.dto.message.JsonMessageBuilder;
import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.ClientRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenterHandler extends TextWebSocketHandler {

    @Autowired
    private MlSideHandler mlSideHandler;
    private Gson gson;
    private ClientRepository clientRepository;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        switch (jsonMessage.get("id").getAsString()) {
            case "presenter":
                try {
                    presenter(session, jsonMessage);
                } catch (Throwable throwable) {
                    handleErrorResponse(throwable, session, "presenterResponse");
                }
                break;
            case "onIceCandidate": {
                JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
                UserSession user = clientRepository.getPresenter(session.getId());
                if (user != null) {
                    IceCandidate cand =
                            new IceCandidate(candidate.get("candidate").getAsString(), candidate.get("sdpMid")
                                    .getAsString(), candidate.get("sdpMLineIndex").getAsInt());
                    user.addCandidate(cand);
                }
                break;
            }
            case "stop":
                stop(session);
                break;
            default:
                break;
        }
    }

    private void presenter(final WebSocketSession session, JsonObject jsonMessage) throws IOException {

        if (clientRepository.getPresenter(session.getId()) == null) {
            clientRepository.putPresenter(session.getId(), UserSession.builder().session(session).build());
            log.info("generating new session..." + session.getId());
        }
        UserSession presenterSession = clientRepository.getPresenter(session.getId());
        presenterSession.setSdpOffer(jsonMessage.get("sdpOffer").getAsString());

        mlSideHandler.sendTargetInfo(session.getId());
    }

    public synchronized void stop(WebSocketSession session) {
        String sessionId = session.getId();
        if (clientRepository.getPresenter(sessionId) != null
                && clientRepository.getPresenter(sessionId).getSession().getId()
                .equals(sessionId)) {

            mlSideHandler.notifyEnd(sessionId);
            clientRepository.removeMediaPipelineBySessionId(sessionId);
            clientRepository.removeListenRelation(sessionId);
            clientRepository.removePresenter(sessionId);
        }
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
        stop(session);
    }
}
