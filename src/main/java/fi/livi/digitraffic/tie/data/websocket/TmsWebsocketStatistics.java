package fi.livi.digitraffic.tie.data.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${controllers.enabled}' != 'false' and '${config.test}' != 'true'")
public class TmsWebsocketStatistics {
    private static final Logger log = LoggerFactory.getLogger(TmsWebsocketStatistics.class);

    private static final Map<WebsocketType, SentStatistics> sentStatisticsMap = new ConcurrentHashMap<>();

    public enum WebsocketType {TMS, SINGLE_TMS}

    @Scheduled(fixedRate = 60000)
    private void logMessageCount() {
        for (final WebsocketType websocketType : WebsocketType.values()) {
            final SentStatistics sentStatistics = sentStatisticsMap.get(websocketType);

            log.info("Sent websocket statistics for webSocketRoadType={} sessions={} messages={}",
                websocketType, sentStatistics != null ? sentStatistics.sessions : 0, sentStatistics != null ? sentStatistics.messages : 0);

            sentStatisticsMap.put(websocketType, new SentStatistics(sentStatistics != null ? sentStatistics.sessions : 0, 0));
        }
    }
    
    public static synchronized void sentTmsWebsocketStatistics(final WebsocketType type, final int sessions, final int messages) {
        final SentStatistics ss = sentStatisticsMap.get(type);

        final SentStatistics newSs = new SentStatistics(sessions, ss == null ? messages : ss.messages + messages);

        sentStatisticsMap.put(type, newSs);
    }

    private static class SentStatistics {
        final int sessions;
        final int messages;

        private SentStatistics(final int sessions, final int messages) {
            this.sessions = sessions;
            this.messages = messages;
        }
    }
}
