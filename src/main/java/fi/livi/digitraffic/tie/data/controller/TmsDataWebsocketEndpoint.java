package fi.livi.digitraffic.tie.data.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import fi.livi.digitraffic.tie.data.websocket.StatusEncoder;
import fi.livi.digitraffic.tie.data.websocket.StatusMessage;
import fi.livi.digitraffic.tie.data.websocket.TmsEncoder;
import fi.livi.digitraffic.tie.data.websocket.TmsMessage;
import fi.livi.digitraffic.tie.data.websocket.TmsWebsocketStatistics;

@ConditionalOnProperty(name = "websocket.tms.enabled")
@ServerEndpoint(value = MetadataApplicationConfiguration.API_V1_BASE_PATH + MetadataApplicationConfiguration.API_PLAIN_WEBSOCKETS_PART_PATH + "/tmsdata",
                encoders = { StatusEncoder.class, TmsEncoder.class})
@Component
public class TmsDataWebsocketEndpoint {
    private static final Logger log = LoggerFactory.getLogger(TmsDataWebsocketEndpoint.class);

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    @OnOpen
    public void onOpen(final Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(final Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(final Throwable t) {
        log.info("exception", t);
    }

    public static void sendMessage(final TmsMessage message) {
        synchronized (sessions) {
            TmsWebsocketStatistics.sentTmsWebsocketStatistics(TmsWebsocketStatistics.WebsocketType.TMS, sessions.size());

            WebsocketEndpoint.sendMessage(log, message, sessions);
        }
    }

    public static void sendStatus() {
        synchronized (sessions) {
            WebsocketEndpoint.sendMessage(log, StatusMessage.OK, sessions);
        }
    }
}
