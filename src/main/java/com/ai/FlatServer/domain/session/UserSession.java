package com.ai.FlatServer.domain.session;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private final List<IceCandidate> candidateList = new ArrayList<>();
    private WebSocketSession session;
    private String uuid;
    private WebRtcEndpoint webRtcEndpoint;
    private String sdpOffer;

    public void sendMessage(JsonObject message) throws IOException {
        log.debug("Sending message from user '{}' : {}", session.getId(), message);
        session.sendMessage(new TextMessage(message.toString()));
    }

    public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
        this.webRtcEndpoint = webRtcEndpoint;

        for (IceCandidate e : candidateList) {
            this.webRtcEndpoint.addIceCandidate(e);
        }
        this.candidateList.clear();
    }

    public void addCandidate(IceCandidate candidate) {
        if (this.webRtcEndpoint != null) {
            webRtcEndpoint.addIceCandidate(candidate);
            return;
        }
        candidateList.add(candidate);
    }
}
