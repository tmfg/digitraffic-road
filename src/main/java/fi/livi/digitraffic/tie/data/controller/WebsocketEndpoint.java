package fi.livi.digitraffic.tie.data.controller;

import java.util.Collection;

import javax.websocket.Session;

import org.slf4j.Logger;

public abstract class WebsocketEndpoint {

    protected WebsocketEndpoint() {}

    protected static void sendMessage(final Logger log, final Object message, final Collection<Session> sessions) {
        for (final Session s : sessions) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("sendMessage " + message + ", session @" + Integer.toHexString(s.hashCode()));
                }
                s.getAsyncRemote().sendObject(message);
            } catch (final Exception ex) {
                log.error("Error sending message", ex);
            }
        }
    }
}
