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
public class MqttDataStatistics {
    private static final Logger log = LoggerFactory.getLogger(MqttDataStatistics.class);

    private static final Map<ConnectionType, Integer> sentStatisticsMap = new ConcurrentHashMap<>();

    public enum ConnectionType {TMS, WEATHER}

    @Scheduled(fixedRate = 60000)
    private void logMessageCount() {
        for (final ConnectionType connectionType : ConnectionType.values()) {
            final Integer sentMessages = sentStatisticsMap.get(connectionType);

            log.info("Sent mqtt statistics for type={} messages={}", connectionType, sentMessages != null ? sentMessages : 0);

            sentStatisticsMap.put(connectionType, 0);
        }
    }

    public static synchronized void sentMqttStatistics(final ConnectionType type, final int messages) {
        if (sentStatisticsMap.containsKey(type)) {
            sentStatisticsMap.put(type, sentStatisticsMap.get(type) + messages);
        } else {
            sentStatisticsMap.put(type, messages);
        }
    }
}
