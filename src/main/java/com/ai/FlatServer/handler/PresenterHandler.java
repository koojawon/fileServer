package com.ai.FlatServer.handler;

import com.ai.FlatServer.domain.dto.message.JsonMessageBuilder;
import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.implement.PresenterRepositoryInMemoryImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@RequiredArgsConstructor
public class PresenterHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();
    private KurentoClient kurento;
    private PresenterRepositoryInMemoryImpl presenterRepository;

    private MlSideHandler mlSideHandler;

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
            case "onIceCandidate": {
                JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
                UserSession user = presenterRepository.getUserSessionBySessionId(session.getId());
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

    private synchronized void presenter(final WebSocketSession session, JsonObject jsonMessage) throws IOException {

        if (presenterRepository.getUserSessionBySessionId(session.getId()) == null) {
            presenterRepository.putUserSessionBySessionId(session.getId(),
                    UserSession.builder().session(session).build());
        }

        UserSession presenterSession = presenterRepository.getUserSessionBySessionId(session.getId());
        presenterRepository.putMediaPipelineBySessionId(session.getId(), kurento.createMediaPipeline());
        presenterSession.setWebRtcEndpoint(
                new WebRtcEndpoint.Builder(
                        presenterRepository.getMediaPipelineBySessionId(session.getId())).useDataChannels()
                        .build());

        WebRtcEndpoint presenterWebRtc = presenterSession.getWebRtcEndpoint();
        presenterWebRtc.addIceCandidateFoundListener(iceCandidateFoundEvent -> {
            try {
                synchronized (session) {
                    session.sendMessage(
                            new TextMessage(new JsonMessageBuilder()
                                    .addProperty("id", "iceCandidate")
                                    .add("candidate",
                                            JsonUtils.toJsonObject(iceCandidateFoundEvent.getCandidate()))
                                    .buildAsString()));
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });

        String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
        String sdpAnswer = presenterWebRtc.processAnswer(sdpOffer);

        synchronized (session) {
            presenterSession.sendMessage(new JsonMessageBuilder()
                    .addProperty("id", "presenterResponse")
                    .addProperty("response", "accepted")
                    .addProperty("sdpAnswer", sdpAnswer)
                    .buildAsJsonObj()
            );
        }
        presenterRepository.makePlaceForListener(session.getId());
        presenterWebRtc.gatherCandidates();
        mlSideHandler.sendTargetInfo(session.getId());
    }

    public synchronized void stop(WebSocketSession session) {
        String sessionId = session.getId();
        if (presenterRepository.getUserSessionBySessionId(sessionId) != null
                && presenterRepository.getUserSessionBySessionId(sessionId).getSession().getId()
                .equals(sessionId)) {

            mlSideHandler.notifyEnded(sessionId);
            presenterRepository.removeMediaPipelineBySessionId(sessionId);
            presenterRepository.removeUserSessionBySessionId(sessionId);
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
