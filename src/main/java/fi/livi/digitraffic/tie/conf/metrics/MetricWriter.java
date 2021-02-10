package fi.livi.digitraffic.tie.conf.metrics;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.tie.aop.NoJobLogging;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;

/**
 * Measure pool statistics every 100ms and log min and max once a minute.
 */
@Configuration
public class MetricWriter {
    private final MeterRegistry meterRegistry;

    private static final Logger LOG = LoggerFactory.getLogger(MetricWriter.class);

    private final List<LoggableMetric> metricsToLog = Arrays.asList(
        new LoggableMetric("process.cpu.usage"),
        new TaggableMetric("jvm.memory", "used", "area"),
        new HikariMetric("max"),
        new HikariMetric("pending"),
        new HikariMetric("active")
    );

    private static volatile Map<String, Pair<Double, Double>> metricMap = new HashMap<>();

    public MetricWriter(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 1000*60, initialDelayString = "${dt.scheduled.job.initialDelay.ms}")
    @NoJobLogging
    void printMetrics() {
        metricMap.keySet().forEach(this::logMeasurement);

        //logAllAvailableMetrics();

        metricMap.clear();
    }

    @Scheduled(fixedRate = 100, initialDelayString = "${dt.scheduled.job.initialDelay.ms}")
    @NoJobLogging
    void updateMetrics() {
        metricsToLog.forEach(metric -> updateMeasurement(metric));
    }

    private void logAllAvailableMetrics() {
        meterRegistry.forEachMeter(m ->
            m.measure().forEach(measure ->
                LOG.info("metric {} measure {}", m.getId(), measure.toString())));
    }

    private Collection<Meter> findMetrics(final LoggableMetric metric) {
        final RequiredSearch requiredSearch = meterRegistry.get(metric.metricKey);

        try {
            return requiredSearch.meters();
        } catch(final Exception e) {
            return null;
        }
    }

    private void updateMeasurement(final LoggableMetric metric) {
        final Collection<Meter> meters = findMetrics(metric);

        if(meters == null) {
            LOG.error("Could not find meter {}", metric.metricKey);
            return;
        }

        meters.forEach(m -> updateMeter(m, metric));
    }

    private void updateMeter(final Meter meter, final LoggableMetric metric) {
        final Measurement measurement = StreamSupport.stream(meter.measure().spliterator(), false)
            .filter(m -> m.getStatistic() == metric.statistic)
            .findFirst().orElse(null);

        if(measurement == null) {
            LOG.error("Could not find statistic {} for {}", metric.statistic, metric.metricKey);
            return;
        }

        final String loggingKey = metric.loggingKey(meter);

        final Pair<Double, Double> oldValue = metricMap.get(loggingKey);
        final Pair<Double, Double> newValue = oldValue == null ? Pair.of(measurement.getValue(), measurement.getValue())
            : Pair.of(Math.max(oldValue.getLeft(), measurement.getValue()), Math.min(oldValue.getRight(), measurement.getValue()));

//        LOG.info(meterName + " old values " + oldValue.getLeft() + " and " + oldValue.getRight());
//        LOG.info(meterName + " new values " + newValue.getLeft() + " and " + newValue.getRight());

        metricMap.put(loggingKey, newValue);
    }

    private void logMeasurement(final String meterName) {
        final Pair<Double, Double> values = metricMap.get(meterName);

        if(values != null) {
            // must set root-locale to use . as decimal separator
            LOG.info(String.format(Locale.ROOT, "meterName=%s.max statisticValue=%.02f", meterName, values.getLeft()));
            LOG.info(String.format(Locale.ROOT, "meterName=%s.min statisticValue=%.02f", meterName, values.getRight()));
        }
    }
}
