package com.ai.FlatServer.repository;

import com.ai.FlatServer.domain.session.UserSession;
import org.kurento.client.MediaPipeline;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository {
    void setListenRelation(String presenterSessionId, String viewerUuid);

    void removeListenRelation(String sessionId);

    void putUser(String id, UserSession session);

    UserSession getUser(String id);

    void removeUser(String id);

    String getViewerWithPresenter(String sessionId);

    MediaPipeline getMediaPipelineBySessionId(String id);

    void putMediaPipelineBySessionId(String id, MediaPipeline pipeline);

    void removeMediaPipelineBySessionId(String id);
}
