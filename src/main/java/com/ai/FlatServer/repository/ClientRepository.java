package com.ai.FlatServer.repository;

import com.ai.FlatServer.domain.session.UserSession;
import org.kurento.client.MediaPipeline;

public interface ClientRepository {
    void setListenRelation(String presenterSessionId, String viewerUuid);

    void removeViewer(String id);

    MediaPipeline getMediaPipelineBySessionId(String id);

    void putMediaPipelineBySessionId(String id, MediaPipeline pipeline);

    void putPresenter(String id, UserSession userSession);

    void removePresenter(String id);

    void removeMediaPipelineBySessionId(String id);

    UserSession getPresenter(String id);

    void removeListenRelation(String sessionId);
}
