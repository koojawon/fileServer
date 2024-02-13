package com.ai.FlatServer.domain.session;

import com.google.gson.JsonObject;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {
    private WebSocketSession session;
    private WebRtcEndpoint webRtcEndpoint;

    public void sendMessage(JsonObject message) throws IOException {
        log.debug("Sending message from user '{}' : {}", session.getId(), message);
        session.sendMessage(new TextMessage(message.toString()));
    }

    public void addCandidate(IceCandidate candidate) {
        webRtcEndpoint.addIceCandidate(candidate);
    }

    public boolean isEmptySession() {
        return session == null && webRtcEndpoint == null;
    }

}
