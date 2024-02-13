package com.ai.FlatServer.repository;

import com.ai.FlatServer.domain.session.UserSession;
import org.kurento.client.MediaPipeline;

public interface PresenterRepository {
    void makePlaceForListener(String id);

    void setListener(String id, UserSession session);

    Boolean getListeningStatus(String id);

    void removeListeningStatus(String id);

    MediaPipeline getMediaPipelineBySessionId(String id);

    void putMediaPipelineBySessionId(String id, MediaPipeline pipeline);

    void putUserSessionBySessionId(String id, UserSession userSession);

    void removeUserSessionBySessionId(String id);

    void removeMediaPipelineBySessionId(String id);

    UserSession getUserSessionBySessionId(String id);
}
