package com.ai.FlatServer.domain.session;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class UserSession {

    private final String name;
    private final WebSocketSession session;
    private final List<IceCandidate> candidateList = new ArrayList<>();
    private String sdpOffer;
    private String callingFrom;
    private String callingTo;
    private WebRtcEndpoint webRtcEndpoint;

    public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
        this.webRtcEndpoint = webRtcEndpoint;
        for (IceCandidate i : candidateList) {
            this.webRtcEndpoint.addIceCandidate(i);
        }
        this.candidateList.clear();
    }

    public void sendMessage(JsonObject message) throws IOException {
        log.debug("Sending message from user '{}' : {}", name, message);
        session.sendMessage(new TextMessage(message.toString()));
    }

    public void addCandidate(IceCandidate candidate) {
        if (this.webRtcEndpoint != null) {
            this.webRtcEndpoint.addIceCandidate(candidate);
        } else {
            this.candidateList.add(candidate);
        }
    }

    public void clear() {
        this.webRtcEndpoint = null;
        this.candidateList.clear();
    }

    public String getSessionId() {
        return session.getId();
    }
}
