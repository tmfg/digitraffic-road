package fi.livi.digitraffic.tie.data.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import fi.livi.digitraffic.tie.data.websocket.StatusEncoder;
import fi.livi.digitraffic.tie.data.websocket.StatusMessage;
import fi.livi.digitraffic.tie.data.websocket.TmsEncoder;
import fi.livi.digitraffic.tie.data.websocket.TmsMessage;

@ConditionalOnProperty(name = "websocket.tms.enabled")
@ServerEndpoint(value = MetadataApplicationConfiguration.API_V1_BASE_PATH + MetadataApplicationConfiguration.API_PLAIN_WEBSOCKETS_PART_PATH + "/tmsdata/{id}",
                encoders = { TmsEncoder.class, StatusEncoder.class})
@Component
public class SingleTmsDataWebsocketEndpoint {
    private static final Logger log = LoggerFactory.getLogger(SingleTmsDataWebsocketEndpoint.class);

    private static final Map<String, Set<Session>> sessions = Collections.synchronizedMap(new HashMap<>());

    @OnOpen
    public void onOpen(final Session session, @PathParam("id") final String roadStationNaturalId) throws IOException {

        if (!NumberUtils.isDigits(roadStationNaturalId)) {
            log.info("method=onOpen Invalid WebSocket pathParameter={} closing connection", roadStationNaturalId);
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid path parameter '" + roadStationNaturalId +"'");
            session.close(reason);
        } else {
            synchronized (sessions) {
                if(!sessions.containsKey(roadStationNaturalId)) {
                    sessions.put(roadStationNaturalId, new HashSet<>());
                }
                sessions.get(roadStationNaturalId).add(session);
            }
        }
    }

    @OnClose
    public void onClose(final Session session, @PathParam("id") final String roadStationNaturalId) {
        synchronized (sessions) {
            final Set<Session> set = sessions.get(roadStationNaturalId);
            if (set != null) {
                set.remove(session);
            }
        }
    }

    public static void sendMessage(final TmsMessage message) {
        synchronized (sessions) {
            removeClosedSessions();
            final Set<Session> sessionSet = sessions.get(Long.toString(message.sensorValue.getRoadStationNaturalId()));

            if(sessionSet != null) {
                log.debug("sessions: {}", sessionSet.size());
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
