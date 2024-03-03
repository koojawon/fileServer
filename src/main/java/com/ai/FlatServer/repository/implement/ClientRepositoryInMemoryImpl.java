package com.ai.FlatServer.repository.implement;

import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.ClientRepository;
import java.util.concurrent.ConcurrentHashMap;
import org.kurento.client.MediaPipeline;
import org.springframework.stereotype.Repository;

@Repository
public class ClientRepositoryInMemoryImpl implements ClientRepository {

    private final ConcurrentHashMap<String, UserSession> viewerSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> presenterSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MediaPipeline> pipelines = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> listenRelations = new ConcurrentHashMap<>();


    public void setListenRelation(String presenterSessionId, String viewerUuid) {
        if (listenRelations.replace(presenterSessionId, viewerUuid) == null) {
            listenRelations.put(presenterSessionId, viewerUuid);
        }
    }

    public void removeListenRelation(String presenterSessionId) {
        listenRelations.remove(presenterSessionId);
    }

    public String getViewerWithPresenter(String sessionId) {
        return listenRelations.get(sessionId);
    }

    public void putViewer(UserSession viewerSession) {
        viewerSessions.put(viewerSession.getUuid(), viewerSession);
    }

    public UserSession getViewer(String uuid) {
        return viewerSessions.get(uuid);
    }

    public void removeViewer(String id) {
        viewerSessions.get(id).getWebRtcEndpoint().release();
        viewerSessions.remove(id);
    }

    public MediaPipeline getMediaPipelineBySessionId(String id) {
        return pipelines.get(id);
    }

    public void putMediaPipelineBySessionId(String id, MediaPipeline pipeline) {
        pipelines.put(id, pipeline);
    }

    public void removeMediaPipelineBySessionId(String id) {
        if (pipelines.get(id) != null) {
            pipelines.get(id).release();
            pipelines.remove(id);
        }
    }

    public UserSession getPresenter(String id) {
        return presenterSessions.get(id);
    }

    public void putPresenter(String id, UserSession userSession) {
        presenterSessions.put(id, userSession);
    }

    public void removePresenter(String id) {
        presenterSessions.remove(id);
    }


}
