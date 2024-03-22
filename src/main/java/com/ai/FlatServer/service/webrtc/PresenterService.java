package com.ai.FlatServer.service.webrtc;

import com.ai.FlatServer.domain.mapper.JsonMessageEncoder;
import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.ClientRepository;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenterService {

    private final ClientRepository clientRepository;

    private final JsonMessageEncoder encoder;

    public void presenter(final WebSocketSession session, JsonObject jsonMessage) {
        if (!jsonMessage.has("fileId")) {
            throw new IllegalArgumentException("Request does not contains file ID!");
        }
        if (clientRepository.getUser(session.getId()) == null) {
            clientRepository.putUser(session.getId(),
                    UserSession.builder()
                            .session(session)
                            .sdpOffer(jsonMessage.get("sdpOffer").getAsString()).build());
            log.info("generating new session... " + session.getId());
        }
    }

    public void stop(WebSocketSession session) {
        String sessionId = session.getId();
        if (clientRepository.getUser(sessionId) != null) {
            clientRepository.removeMediaPipelineBySessionId(sessionId);
            clientRepository.removeListenRelation(sessionId);
            clientRepository.removeUser(sessionId);
        }
    }

    public void iceCandidateReceived(JsonObject jsonMessage, WebSocketSession session) {
        JsonObject candidateMessage = jsonMessage.get("candidate").getAsJsonObject();
        UserSession user = clientRepository.getUser(session.getId());
        if (user != null) {
            IceCandidate candidate =
                    new IceCandidate(candidateMessage.get("candidate").getAsString(),
                            candidateMessage.get("sdpMid").getAsString(),
                            candidateMessage.get("sdpMLineIndex").getAsInt());
            user.addCandidate(candidate);
        }
    }

    public String initPresenter(String id) {
        UserSession presenterSession = clientRepository.getUser(id);
        if (presenterSession == null) {
            throw new NoSuchElementException("No such presenter exists!");
        }
        presenterSession.setWebRtcEndpoint(createPresenterEndpoint(presenterSession));
        log.info("presenter initialized");
        return id;
    }

    private WebRtcEndpoint createPresenterEndpoint(UserSession session) {
        log.info("creating presenter endpoint");
        WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(
                clientRepository.getMediaPipelineBySessionId(session.getSession().getId()))
                .useDataChannels()
                .build();

        webRtcEndpoint.addIceCandidateFoundListener(iceCandidateFoundEvent -> {
            JsonObject response = encoder.toIceCandidateJsonObject(iceCandidateFoundEvent.getCandidate());
            try {
                synchronized (session) {
                    session.sendMessage(response);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        return webRtcEndpoint;
    }

    public void processOffer(String id) throws IOException {
        UserSession presenterSession = clientRepository.getUser(id);
        log.info(presenterSession.toString());
        String answer = presenterSession.getWebRtcEndpoint()
                .processOffer(clientRepository.getUser(id).getSdpOffer());
        log.info(answer);
        synchronized (presenterSession.getSession()) {
            presenterSession.sendMessage(encoder.toPresenterResponse(answer));
        }
    }

    public void startGathering(String presenterId) {
        UserSession presenter = clientRepository.getUser(presenterId);
        presenter.getWebRtcEndpoint().gatherCandidates();
    }
}
