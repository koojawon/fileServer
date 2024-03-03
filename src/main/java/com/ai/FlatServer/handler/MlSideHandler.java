package com.ai.FlatServer.handler;

import com.ai.FlatServer.domain.dto.message.IceCandidateMessage;
import com.ai.FlatServer.domain.dto.message.TargetInfoResponseMessage;
import com.ai.FlatServer.domain.mapper.JsonMessageDecoder;
import com.ai.FlatServer.domain.mapper.JsonMessageEncoder;
import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.implement.ClientRepositoryInMemoryImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MlSideHandler {

    private final RabbitTemplate rabbitTemplate;
    private final ClientRepositoryInMemoryImpl clientRepository;
    private final JsonMessageEncoder encoder;
    private final JsonMessageDecoder decoder;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Autowired
    private KurentoClient kurentoClient;

    public void sendTargetInfo(String targetId) {
        String json = encoder.toTargetInfoMessage(targetId);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, json);
    }

    @RabbitListener(queues = "pdfJobQueue.senderQueue")
    public void handleMessage(String message) throws IOException {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        switch (jsonObject.get("id").getAsString()) {
            case "viewer" -> viewer(decoder.toTargetInfoResponseMessage(jsonObject));
            case "onIceCandidate" -> onIceCandidate(decoder.toIceCandidateMessage(jsonObject));
        }
    }

    private void onIceCandidate(IceCandidateMessage message) {
        IceCandidate candidate = new IceCandidate(message.getCandidate(), message.getSdpMid(),
                message.getSdpMLineIndex());
        clientRepository.getViewer(message.getUuid()).addCandidate(candidate);
    }

    private void viewer(TargetInfoResponseMessage message) throws IOException {
        if (clientRepository.getPresenter(message.getTargetId()) == null) {
            String json = encoder.toRejectMessage();
            rabbitTemplate.convertAndSend(exchangeName, routingKey, json);
        } else {
            UserSession mlSession = UserSession.builder().uuid(message.getUuid()).build();
            UserSession presenterSession = clientRepository.getPresenter(message.getTargetId());

            mlSession.setSdpOffer(message.getSdpOffer());

            clientRepository.putMediaPipelineBySessionId(presenterSession.getSession().getId(),
                    kurentoClient.createMediaPipeline());

            MediaPipeline m = clientRepository.getMediaPipelineBySessionId(presenterSession.getSession().getId());

            m.setLatencyStats(true);

            presenterSession.setWebRtcEndpoint(
                    new WebRtcEndpoint.Builder(
                            clientRepository.getMediaPipelineBySessionId(presenterSession.getSession().getId()))
                            .useDataChannels()
                            .build());
            WebRtcEndpoint presenterWebRtc = presenterSession.getWebRtcEndpoint();
            WebRtcEndpoint viewerWebRtc = new WebRtcEndpoint.Builder(
                    clientRepository.getMediaPipelineBySessionId(message.getTargetId())).useDataChannels().build();

            presenterWebRtc.addIceCandidateFoundListener(iceCandidateFoundEvent -> {
                JsonObject response = encoder.toIceCandidateJsonObject(iceCandidateFoundEvent.getCandidate());
                try {
                    synchronized (presenterSession.getSession()) {
                        presenterSession.sendMessage(response);
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            });

            viewerWebRtc.addIceCandidateFoundListener(event -> {
                String json = encoder.toIceCandidateMessage(event.getCandidate());
                try {
                    rabbitTemplate.convertAndSend(exchangeName, routingKey, json);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });

            mlSession.setWebRtcEndpoint(viewerWebRtc);
            presenterWebRtc.connect(viewerWebRtc);
            viewerWebRtc.connect(presenterWebRtc);

            String presenterSdpAnswer = presenterWebRtc.processOffer(presenterSession.getSdpOffer());
            String viewerSdpAnswer = viewerWebRtc.processOffer(message.getSdpOffer());

            synchronized (presenterSession.getSession()) {
                presenterSession.sendMessage(encoder.toPresenterResponse(presenterSdpAnswer));
            }

            String json = encoder.toViewerResponse(viewerSdpAnswer);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, json);

            presenterWebRtc.gatherCandidates();
            viewerWebRtc.gatherCandidates();
            clientRepository.setListenRelation(message.getTargetId(), mlSession.getUuid());
            clientRepository.putViewer(mlSession);
        }
    }

    public void notifyEnd(String id) {
        String uuid = clientRepository.getViewerWithPresenter(id);
        if (uuid != null) {
            String json = encoder.toEndMessage(id);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, json);
            clientRepository.removeViewer(uuid);
        }
    }
}
