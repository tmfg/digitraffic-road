package fi.livi.digitraffic.tie.conf.metrics;

import fi.livi.digitraffic.tie.aop.NoJobLogging;
import fi.livi.digitraffic.tie.aop.TransactionLoggerAspect;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PendingConnectionDebugger {
    private final MeterRegistry meterRegistry;

    private static final Logger log = LoggerFactory.getLogger("PendingConnectionDebugger");

    public PendingConnectionDebugger(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 50)
    @NoJobLogging
    void debugPendingConnections() {
        final RequiredSearch requiredSearch = meterRegistry.get(HikariCPMetrics.CONNECTIONS_PENDING);
        final Meter meter = requiredSearch.meter(); // should only have one meter
        final Measurement measurement = meter.measure().iterator().next(); // should only have one measurement

        // when connections are pending, print all active transactions
        if(measurement.getValue() > 0) {
            TransactionLoggerAspect.logActiveTransactions(log);
        }
    }
}
