package com.ai.FlatServer.repository.implement;

import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.PresenterRepository;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.kurento.client.MediaPipeline;
import org.springframework.stereotype.Repository;

@Repository
public class PresenterRepositoryInMemoryImpl implements PresenterRepository {

    private final ConcurrentHashMap<String, UserSession> listeningSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> presenterSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MediaPipeline> pipelines = new ConcurrentHashMap<>();

    public void makePlaceForListener(String id) {
        listeningSessions.put(id, new UserSession());
    }

    public void setListener(String id, UserSession session) {
        if (listeningSessions.replace(id, session) == null) {
            throw new NoSuchElementException("No such session exists!!");
        }
    }

    public Boolean getListeningStatus(String id) {
        return listeningSessions.get(id).isEmptySession();
    }

    public void removeListeningStatus(String id) {
        listeningSessions.get(id).getWebRtcEndpoint().release();
        listeningSessions.remove(id);
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

    public UserSession getUserSessionBySessionId(String id) {
        return presenterSessions.get(id);
    }

    public void putUserSessionBySessionId(String id, UserSession userSession) {
        presenterSessions.put(id, userSession);
    }


    public void removeUserSessionBySessionId(String id) {
        presenterSessions.remove(id);
    }

}
