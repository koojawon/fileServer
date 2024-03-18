package com.ai.FlatServer.repository.implement;

import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.ClientRepository;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.kurento.client.MediaPipeline;
import org.springframework.stereotype.Repository;

@Repository
public class ClientRepositoryInMemoryImpl implements ClientRepository {

    private final ConcurrentHashMap<String, UserSession> viewerSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> presenterSessions = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, UserSession> userSessions = new ConcurrentHashMap<>();
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

    @Override
    public void putUser(String id, UserSession session) {
        userSessions.put(id, session);
    }

    @Override
    public UserSession getUser(String id) {
        return userSessions.get(id);
    }

    @Override
    public void removeUser(String id) {
        if (userSessions.get(id) != null) {
            userSessions.get(id).getWebRtcEndpoint().release();
            userSessions.remove(id);
            return;
        }
        throw new NoSuchElementException();
    }

    public String getViewerWithPresenter(String sessionId) {
        return listenRelations.get(sessionId);
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
}
