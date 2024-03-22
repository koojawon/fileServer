package com.ai.FlatServer.service.webrtc;

import com.ai.FlatServer.domain.dto.message.IceCandidateMessage;
import com.ai.FlatServer.domain.dto.message.TargetInfoResponseMessage;
import com.ai.FlatServer.domain.mapper.JsonMessageEncoder;
import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.ClientRepository;
import com.ai.FlatServer.service.MessageService;
import com.google.gson.JsonObject;
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
public class ViewerService {

    private final JsonMessageEncoder encoder;

    private final MessageService messageService;
    private final ClientRepository clientRepository;

    public void sendTargetInfo(String targetId, JsonObject jsonMessage) {
        String fileName = jsonMessage.get("fileId").getAsString();
        JsonObject jsonObject = encoder.toTargetInfoMessage(targetId, fileName);
        messageService.sendMessage(jsonObject);
    }

    public void notifyEnd(WebSocketSession session) {
        String uuid = clientRepository.getViewerWithPresenter(session.getId());
        if (uuid != null) {
            JsonObject jsonObject = encoder.toEndMessage(session.getId());
            messageService.sendMessage(jsonObject);
            clientRepository.removeUser(uuid);
        }
    }

    public void onIceCandidate(IceCandidateMessage message) {
        IceCandidate candidate = new IceCandidate(message.getCandidate(), message.getSdpMid(),
                message.getSdpMLineIndex());
        clientRepository.getUser(message.getUuid()).addCandidate(candidate);
    }

    public void reject() {
        JsonObject jsonObject = encoder.toRejectMessage();
        messageService.sendMessage(jsonObject);
    }

    public String initViewer(TargetInfoResponseMessage message) {
        if (clientRepository.getUser(message.getTargetId()) == null) {
            reject();
            throw new NoSuchElementException("No such presenter exists!");
        } else {
            UserSession viewerSession = UserSession.builder().uuid(message.getUuid()).build();
            viewerSession.setSdpOffer(message.getSdpOffer());
            clientRepository.putUser(viewerSession.getUuid(), viewerSession);

            WebRtcEndpoint viewerWebRtc = createViewerEndpoint(message.getTargetId());
            viewerSession.setWebRtcEndpoint(viewerWebRtc);
            log.info("created new viewer :" + viewerSession.getUuid());
            return viewerSession.getUuid();
        }
    }

    private WebRtcEndpoint createViewerEndpoint(String id) {
        WebRtcEndpoint viewerWebRtc = new WebRtcEndpoint.Builder(
                clientRepository.getMediaPipelineBySessionId(id)).useDataChannels().build();

        viewerWebRtc.addIceCandidateFoundListener(event -> {
            JsonObject jsonObject = encoder.toIceCandidateMessage(event.getCandidate());
            try {
                messageService.sendMessage(jsonObject);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
        return viewerWebRtc;
    }

    public void processOffer(String id) {
        UserSession viewer = clientRepository.getUser(id);
        WebRtcEndpoint viewerEndpoint = viewer.getWebRtcEndpoint();
        String answer = viewerEndpoint.processOffer(viewer.getSdpOffer());
        JsonObject jsonObject = encoder.toViewerResponse(answer);
        log.info(answer);
        messageService.sendMessage(jsonObject);
    }

    public void startGathering(String viewerId) {
        UserSession viewer = clientRepository.getUser(viewerId);
        viewer.getWebRtcEndpoint().gatherCandidates();
    }
}
