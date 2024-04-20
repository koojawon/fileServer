package com.ai.FlatServer.webrtc.controller;

import com.ai.FlatServer.global.rabbitmq.mapper.JsonMessageDecoder;
import com.ai.FlatServer.webrtc.message.TargetInfoResponseMessage;
import com.ai.FlatServer.webrtc.service.MediaPipelineService;
import com.ai.FlatServer.webrtc.service.PresenterService;
import com.ai.FlatServer.webrtc.service.ViewerService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewerController {

    private final JsonMessageDecoder decoder;

    private final ViewerService viewerService;

    private final PresenterService presenterService;

    private final MediaPipelineService mediaPipelineService;

    @RabbitListener(queues = "pdfJobQueue.senderQueue")
    public void handleMessage(String message) throws IOException {
        log.info(message);
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

        switch (jsonObject.get("id").getAsString()) {
            case "viewer" -> handleViewerMessage(jsonObject);
            case "onIceCandidate" -> viewerService.onIceCandidate(decoder.toIceCandidateMessage(jsonObject));
        }
    }


    private void handleViewerMessage(JsonObject jsonObject) {
        try {

            TargetInfoResponseMessage message = decoder.toTargetInfoResponseMessage(jsonObject);
            mediaPipelineService.createMediaPipeline(message.getTargetId());
            String viewerId = viewerService.initViewer(message);
            mediaPipelineService.enableStatsOfId(message.getTargetId());
            String presenterId = presenterService.initPresenter(message.getTargetId());
            mediaPipelineService.connectEach(presenterId, viewerId);

            presenterService.processOffer(message.getTargetId());

            viewerService.processOffer(viewerId);

            viewerService.startGathering(viewerId);
            presenterService.startGathering(presenterId);

            mediaPipelineService.setRelation(presenterId, viewerId);
        } catch (Exception e) {
            log.error(e.toString());
        }

    }
}
