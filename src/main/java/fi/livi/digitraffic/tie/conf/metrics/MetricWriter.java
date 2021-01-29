package fi.livi.digitraffic.tie.conf.metrics;

import fi.livi.digitraffic.tie.aop.NoJobLogging;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Measure pool statistics every 100ms and log min and max once a minute.
 */
@Configuration
public class MetricWriter {
    private final MeterRegistry meterRegistry;

    private static final Logger LOG = LoggerFactory.getLogger(MetricWriter.class);

    private final List<String> meterNames = Arrays.asList(
        "hikaricp.connections.max",
        "hikaricp.connections.pending",
        "hikaricp.connections.active");

    private static volatile Map<String, Pair<Double, Double>> metricMap = new HashMap<>();

    public MetricWriter(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 1000*60)
    @NoJobLogging
    void printMetrics() {
        meterNames.forEach(this::logMeasurement);

        metricMap.clear();
    }

    @Scheduled(fixedRate = 100)
    @NoJobLogging
    void updateMetrics() {
        meterNames.forEach(name -> updateMeasurement(name, Statistic.VALUE));
    }

    private void updateMeasurement(final String meterName, final Statistic statistic) {
        final Meter meter = meterRegistry.get(meterName).meter();

        if(meter == null) {
            LOG.error("Could not find meter {}", meterName);
            return;
        }

        final Measurement measurement = StreamSupport.stream(meter.measure().spliterator(), false)
            .filter(m -> m.getStatistic() == statistic)
            .findFirst().orElse(null);

        if(measurement == null) {
            LOG.error("Could not find statistic {} for {}", statistic, meterName);
            return;
        }

        final Pair<Double, Double> oldValue = metricMap.computeIfAbsent(meterName, x -> Pair.of(0.0, 0.0));
        final Pair<Double, Double> newValue = Pair.of(Math.max(oldValue.getLeft(), measurement.getValue()),
            Math.min(oldValue.getRight(), measurement.getValue()));

//        LOG.info(meterName + " old values " + oldValue.getLeft() + " and " + oldValue.getRight());
//        LOG.info(meterName + " new values " + newValue.getLeft() + " and " + newValue.getRight());

        metricMap.put(meterName, newValue);
    }

    private void logMeasurement(final String meterName) {
        final Pair<Double, Double> values = metricMap.get(meterName);

        if(values != null) {
            LOG.info("meterName={}.max statisticValue={}", meterName, values.getLeft());
            LOG.info("meterName={}.min statisticValue={}", meterName, values.getRight());
        }
    }
}
