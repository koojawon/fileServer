package com.ai.FlatServer.service.webrtc;

import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.ClientRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaPipelineService {
    private final KurentoClient kurentoClient;
    private final ClientRepository clientRepository;

    public void createMediaPipeline(String id) {
        UserSession presenterSession = clientRepository.getUser(id);
        if (presenterSession == null) {
            throw new NoSuchElementException("No such presenter Exists!!");
        }

        clientRepository.putMediaPipelineBySessionId(presenterSession.getSession().getId(),
                kurentoClient.createMediaPipeline());
        log.info("pipeline constructed");
    }

    public void enableStatsOfId(String id) {
        clientRepository.getMediaPipelineBySessionId(id).setLatencyStats(true);
        log.info("enabled stat");
    }

    public void connectEach(String presenterId, String viewerId) {
        WebRtcEndpoint presenterEndpoint = clientRepository.getUser(presenterId).getWebRtcEndpoint();
        WebRtcEndpoint viewerEndpoint = clientRepository.getUser(viewerId).getWebRtcEndpoint();

        presenterEndpoint.connect(viewerEndpoint);
        viewerEndpoint.connect(presenterEndpoint);
        log.info("connected" + viewerId + " and " + presenterId);
    }

    public void setRelation(String presenterId, String viewerId) {
        clientRepository.setListenRelation(presenterId, viewerId);
    }
}
