package com.ai.FlatServer.handler;

import com.ai.FlatServer.domain.pipeline.CallMediaPipeline;
import com.ai.FlatServer.domain.session.UserSession;
import com.ai.FlatServer.repository.UserRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
public class CallHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();

    private static final ConcurrentHashMap<String, CallMediaPipeline> pipelines = new ConcurrentHashMap<String, CallMediaPipeline>();

    @Autowired
    private KurentoClient kurento;

    @Autowired
    private UserRegistry registry;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        UserSession user = registry.getBySession(session);

        if (user != null) {
            log.debug("Incoming message from user '{}': {}", user.getName(), jsonMessage);
        } else {
            log.debug("Incoming message from new user: {}", jsonMessage);
        }

        switch (jsonMessage.get("id").getAsString()) {
            case "register":
                try {
                    register(session, jsonMessage);
                } catch (Throwable t) {
                    handleErrorResponse(t, session, "RegisterResponse");
                }
                break;
            case "call":
                try {
                    call(user, jsonMessage);
                } catch (Throwable t) {
                    handleErrorResponse(t, session, "CallResponse");
                }
                break;
            case "incomingCallResponse":
                incomingCallResponse(user, jsonMessage);
                break;
            case "onIceCandidate": {
                JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
                if (user != null) {
                    IceCandidate cand =
                            new IceCandidate(candidate.get("candidate").getAsString(), candidate.get("sdpMid")
                                    .getAsString(), candidate.get("sdpMLineIndex").getAsInt());
                    user.addCandidate(cand);
                }
                break;
            }
            case "stop":
                stop(session);
                break;
            default:
                break;
        }


    }

    public void stop(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        if (pipelines.containsKey(sessionId)) {
            pipelines.get(sessionId).release();
            CallMediaPipeline pipeline = pipelines.remove(sessionId);
            pipeline.release();

            UserSession stopperUser = registry.getBySession(session);
            if (stopperUser != null) {
                UserSession stoppedUser =
                        (stopperUser.getCallingFrom() != null) ? registry.getByName(stopperUser
                                .getCallingFrom()) : stopperUser.getCallingTo() != null ? registry
                                .getByName(stopperUser.getCallingTo()) : null;

                if (stoppedUser != null) {
                    JsonObject message = new JsonObject();
                    message.addProperty("id", "stopCommunication");
                    stoppedUser.sendMessage(message);
                    stoppedUser.clear();
                }
                stopperUser.clear();
            }

        }
    }

    private void handleErrorResponse(Throwable throwable, WebSocketSession session, String responseId)
            throws IOException {
        log.error(throwable.getMessage(), throwable);
        JsonObject response = new JsonObject();

        response.addProperty("id", responseId);
        response.addProperty("response", "rejected");
        response.addProperty("message", throwable.getMessage());
        session.sendMessage(new TextMessage(response.toString()));
    }

    private void register(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        String name = jsonMessage.getAsJsonPrimitive("name").getAsString();

        UserSession caller = new UserSession(name, session);
        String responseMsg = "Accepted";
        if (name.isEmpty()) {
            responseMsg = "rejected: empty user name";
        } else if (registry.exists(name)) {
            responseMsg = "rejected: user '" + name + "' already registered";
        } else {
            registry.register(caller);
        }

        JsonObject response = new JsonObject();
        response.addProperty("id", "registerResponse");
        response.addProperty("response", responseMsg);
        caller.sendMessage(response);
    }

    private void call(UserSession caller, JsonObject jsonMessage) throws IOException {
        String to = jsonMessage.get("to").getAsString();
        String from = jsonMessage.get("from").getAsString();

        JsonObject response = new JsonObject();

        if (registry.exists(to)) {
            caller.setSdpOffer(jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString());
            caller.setCallingFrom(to);

            response.addProperty("id", "incomingCall");
            response.addProperty("from", from);

            UserSession callee = registry.getByName(to);
            callee.sendMessage(response);
            callee.setCallingFrom(from);
        } else {
            response.addProperty("id", "callResponse");
            response.addProperty("response", "rejected: user '" + to + "' is not registerd");
            caller.sendMessage(response);
        }
    }

    private void incomingCallResponse(final UserSession callee, JsonObject jsonMessage) throws IOException {
        String callResponse = jsonMessage.get("callResponse").getAsString();
        String from = jsonMessage.get("from").getAsString();
        final UserSession caller = registry.getByName(from);
        String to = caller.getCallingTo();

        if ("accept".equals(callResponse)) {
            log.debug("Accepted call from '{}' to '{}'", from, to);

            CallMediaPipeline pipeline = null;
            try {
                pipeline = new CallMediaPipeline(kurento);
                pipelines.put(caller.getSessionId(), pipeline);
                pipelines.put(callee.getSessionId(), pipeline);

                callee.setWebRtcEndpoint(pipeline.getCalleeWebRtcEp());
                pipeline.getCalleeWebRtcEp().addIceCandidateFoundListener(
                        event -> {
                            JsonObject response = new JsonObject();
                            response.addProperty("id", "iceCandidate");
                            response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                            try {
                                synchronized (callee.getSession()) {
                                    callee.getSession().sendMessage(new TextMessage(response.toString()));
                                }
                            } catch (IOException e) {
                                log.debug(e.getMessage());
                            }
                        });

                caller.setWebRtcEndpoint(pipeline.getCallerWebRtcEp());
                pipeline.getCallerWebRtcEp().addIceCandidateFoundListener(
                        event -> {
                            JsonObject response = new JsonObject();
                            response.addProperty("id", "iceCandidate");
                            response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                            try {
                                synchronized (caller.getSession()) {
                                    caller.getSession().sendMessage(new TextMessage(response.toString()));
                                }
                            } catch (IOException e) {
                                log.debug(e.getMessage());
                            }
                        });

                String calleeSdpOffer = jsonMessage.get("sdpOffer").getAsString();
                String calleeSdpAnswer = pipeline.generateSdpAnswerForCallee(calleeSdpOffer);
                JsonObject startCommunication = new JsonObject();
                startCommunication.addProperty("id", "startCommunication");
                startCommunication.addProperty("sdpAnswer", calleeSdpAnswer);

                synchronized (callee) {
                    callee.sendMessage(startCommunication);
                }

                pipeline.getCalleeWebRtcEp().gatherCandidates();

                String callerSdpOffer = registry.getByName(from).getSdpOffer();
                String callerSdpAnswer = pipeline.generateSdpAnswerForCaller(callerSdpOffer);
                JsonObject response = new JsonObject();
                response.addProperty("id", "callResponse");
                response.addProperty("response", "accepted");
                response.addProperty("sdpAnswer", callerSdpAnswer);

                synchronized (caller) {
                    caller.sendMessage(response);
                }

                pipeline.getCallerWebRtcEp().gatherCandidates();

            } catch (Throwable t) {
                log.error(t.getMessage(), t);

                if (pipeline != null) {
                    pipeline.release();
                }

                pipelines.remove(caller.getSessionId());
                pipelines.remove(callee.getSessionId());

                JsonObject response = new JsonObject();
                response.addProperty("id", "callResponse");
                response.addProperty("response", "rejected");
                caller.sendMessage(response);

                response = new JsonObject();
                response.addProperty("id", "stopCommunication");
                callee.sendMessage(response);
            }

        } else {
            JsonObject response = new JsonObject();
            response.addProperty("id", "callResponse");
            response.addProperty("response", "rejected");
            caller.sendMessage(response);
        }

    }
}
