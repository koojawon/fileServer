package com.ai.FlatServer.domain.pipeline;

import lombok.Getter;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

public class CallMediaPipeline {

    private MediaPipeline pipeline;

    @Getter
    private WebRtcEndpoint callerWebRtcEp;
    @Getter
    private WebRtcEndpoint calleeWebRtcEp;

    public CallMediaPipeline(KurentoClient kurento) {
        try {
            this.pipeline = kurento.createMediaPipeline();
            this.calleeWebRtcEp = new WebRtcEndpoint.Builder(pipeline).useDataChannels().build();
            this.callerWebRtcEp = new WebRtcEndpoint.Builder(pipeline).useDataChannels().build();

            this.callerWebRtcEp.connect(this.calleeWebRtcEp);
            this.calleeWebRtcEp.connect(this.callerWebRtcEp);
        } catch (Throwable throwable) {
            if (pipeline != null) {
                pipeline.release();
            }
        }
    }

    public String generateSdpAnswerForCaller(String sdpOffer) {
        return callerWebRtcEp.processOffer(sdpOffer);
    }

    public String generateSdpAnswerForCallee(String sdpOffer) {
        return calleeWebRtcEp.processOffer(sdpOffer);
    }

    public void release() {
        if (pipeline != null) {
            pipeline.release();
        }
    }

}
