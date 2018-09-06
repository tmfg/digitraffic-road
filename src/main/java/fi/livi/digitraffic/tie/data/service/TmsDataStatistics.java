package fi.livi.digitraffic.tie.data.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${app.type}' == 'daemon' and '${config.test}' != 'true'")
public class TmsDataStatistics {
    private static final Logger log = LoggerFactory.getLogger(TmsDataStatistics.class);

    private static final Map<ConnectionType, SentStatistics> sentStatisticsMap = new ConcurrentHashMap<>();

    public enum ConnectionType {MQTT, WS_TMS, WS_SINGLE_TMS}

    @Scheduled(fixedRate = 60000)
    private void logMessageCount() {
        for (final ConnectionType connectionType : ConnectionType.values()) {
            final SentStatistics sentStatistics = sentStatisticsMap.get(connectionType);

            log.info("Sent tms statistics for connectionType={} sessions={} messages={}",
                connectionType, sentStatistics != null ? sentStatistics.sessions : 0, sentStatistics != null ? sentStatistics.messages : 0);

            sentStatisticsMap.put(connectionType, new SentStatistics(sentStatistics != null ? sentStatistics.sessions : 0, 0));
        }
    }

    public static synchronized void sentTmsStatistics(final ConnectionType type, final int sessions, final int messages) {
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
