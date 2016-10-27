package fi.livi.digitraffic.tie.data.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import fi.livi.digitraffic.tie.data.websocket.LAMEncoder;
import fi.livi.digitraffic.tie.data.websocket.LAMMessage;
import fi.livi.digitraffic.tie.data.websocket.StatusEncoder;
import fi.livi.digitraffic.tie.data.websocket.StatusMessage;

@ConditionalOnProperty(name = "websocket.lam.enabled")
@ServerEndpoint(value = MetadataApplicationConfiguration.API_V1_BASE_PATH + MetadataApplicationConfiguration.API_PLAIN_WEBSOCKETS_PART_PATH + "/tmsdata/{id}",
                encoders = { LAMEncoder.class, StatusEncoder.class})
@Component
public class LamDataWebsocketEndpoint extends WebsocketEndpoint {
    private static final Logger log = LoggerFactory.getLogger(LamDataWebsocketEndpoint.class);

    private static final Map<Long, Set<Session>> sessions = Collections.synchronizedMap(new HashMap<>());

    @OnOpen
    public void onOpen(final Session session, @PathParam("id") final Long roadStationNaturalId) {
        synchronized (sessions) {
            if(!sessions.containsKey(roadStationNaturalId)) {
                sessions.put(roadStationNaturalId, new HashSet<>());
            }
            sessions.get(roadStationNaturalId).add(session);
        }
    }

    @OnClose
    public void onClose(final Session session, @PathParam("id") final Integer roadStationNaturalId) {
        synchronized (sessions) {
            sessions.get(roadStationNaturalId).remove(session);
        }
    }

    public static void sendMessage(final LAMMessage message) {
        synchronized (sessions) {
            removeClosedSessions();
            final Set<Session> sessionSet = sessions.get(message.sensorValue.getRoadStationNaturalId());

            if(sessionSet != null) {
                log.info("sessions: " + sessionSet.size());
                WebsocketEndpoint.sendMessage(log, message, sessionSet);
            }
        }
    }

    public static void sendStatus() {
        synchronized (sessions) {
            removeClosedSessions();
            WebsocketEndpoint.sendMessage(log, StatusMessage.OK, sessions.values().stream().flatMap(c -> c.stream()).collect(Collectors.toList()));
        }
    }


    private static void removeClosedSessions() {
        for (Set<Session> sSet : sessions.values()) {
            sSet.removeIf(s -> !s.isOpen());
        }
    }
}
