package com.ai.FlatServer.handler;

import com.ai.FlatServer.domain.dto.message.IceCandidateMessage;
import com.ai.FlatServer.domain.dto.message.TargetInfoResponseMessage;
import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.implement.PresenterRepositoryInMemoryImpl;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlSideHandler {

    private final RabbitTemplate rabbitTemplate;
    private final PresenterRepositoryInMemoryImpl presenterRepository;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    private UserSession mlSession;

    public void sendTargetInfo(String targetId) {

        JsonObject response = new JsonObject();
        response.addProperty("id", "targetInfo");
        response.addProperty("targetId", targetId);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, response);
    }

    @RabbitListener(queues = "pdfJobQueue.listenerQueue")
    public void startViewingTarget(TargetInfoResponseMessage message) {
        viewer(message);
    }

    @RabbitListener(queues = "pdfJobQueue.listenerQueue")
    public void onIceCandidateMessage(IceCandidateMessage message) {
        onIceCandidate(message);
    }

    private void onIceCandidate(IceCandidateMessage message) {
        IceCandidate candidate = new IceCandidate(message.getCandidate(), message.getSdpMid(),
                message.getSdpMLineIndex());
        mlSession.addCandidate(candidate);
    }

    private synchronized void viewer(TargetInfoResponseMessage message) {
        if (presenterRepository.getUserSessionBySessionId(message.getTargetId()) == null
                || presenterRepository.getUserSessionBySessionId(message.getTargetId()).getWebRtcEndpoint() == null) {
            JsonObject response = new JsonObject();
            response.addProperty("id", "viewerResponse");
            response.addProperty("response", "rejected");
            response.addProperty("message", "No such sender.");
            rabbitTemplate.convertAndSend(exchangeName, routingKey, response);
        } else {
            if (presenterRepository.getListeningStatus(message.getTargetId())) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "viewerResponse");
                response.addProperty("response", "rejected");
                response.addProperty("message", "already listening to this session.");
                rabbitTemplate.convertAndSend(exchangeName, routingKey, response);
                return;
            }
            mlSession = new UserSession();

            WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(
                    presenterRepository.getMediaPipelineBySessionId(message.getTargetId())).useDataChannels().build();

            nextWebRtc.addIceCandidateFoundListener(event -> {
                JsonObject response = new JsonObject();
                response.addProperty("id", "iceCandidate");
                response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                try {
                    rabbitTemplate.convertAndSend(exchangeName, routingKey, response);
                } catch (Exception e) {
                    log.debug(e.getMessage());
                }
            });

            mlSession.setWebRtcEndpoint(nextWebRtc);
            presenterRepository.getUserSessionBySessionId(message.getTargetId()).getWebRtcEndpoint()
                    .connect(nextWebRtc);
            String sdpOffer = message.getSdpOffer();
            String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

            JsonObject response = new JsonObject();
            response.addProperty("id", "viewerResponse");
            response.addProperty("response", "accepted");
            response.addProperty("sdpAnswer", sdpAnswer);

            rabbitTemplate.convertAndSend(exchangeName, routingKey, response);

            nextWebRtc.gatherCandidates();
            presenterRepository.setListener(message.getTargetId(), mlSession);
        }
    }

    public void notifyEnded(String id) {
        if (presenterRepository.getListeningStatus(id)) {
            JsonObject response = new JsonObject();
            response.addProperty("id", "stopCommunication");
            response.addProperty("targetId", id);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, response);
            presenterRepository.removeListeningStatus(id);
        }
    }
}
