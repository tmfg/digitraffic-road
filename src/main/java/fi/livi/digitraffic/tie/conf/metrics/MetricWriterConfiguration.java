package fi.livi.digitraffic.tie.conf.metrics;

import static fi.livi.digitraffic.common.config.metrics.HikariCPMetrics.CONNECTIONS_ACTIVE;
import static fi.livi.digitraffic.common.config.metrics.HikariCPMetrics.CONNECTIONS_MAX;
import static fi.livi.digitraffic.common.config.metrics.HikariCPMetrics.CONNECTIONS_PENDING;
import static fi.livi.digitraffic.common.config.metrics.HikariCPMetrics.CONNECTIONS_TIMEOUT;
import static fi.livi.digitraffic.common.config.metrics.HikariCPMetrics.TAG_POOL;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.common.annotation.NoJobLogging;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;

/**
 * Measure pool statistics every 100ms and log min and max once a minute.
 */
@ConditionalOnExpression("'${config.test}' != 'true'")
@Configuration
public class MetricWriterConfiguration implements MetricVisitor {
    private final MeterRegistry meterRegistry;

    private static final Logger LOG = LoggerFactory.getLogger(MetricWriterConfiguration.class);

    private final List<LoggableMetric> metricsToLog = Arrays.asList(
        GaugeMetric.of("process.cpu.usage"),
        GaugeMetric.of("system.cpu.count"),
        GaugeMetric.of("jvm.memory.used").withTag("area"),
        GaugeMetric.of(CONNECTIONS_MAX).withTag(TAG_POOL).noMin(),
        CountMetric.of(CONNECTIONS_TIMEOUT).withTag(TAG_POOL),
        GaugeMetric.of(CONNECTIONS_PENDING).withTag(TAG_POOL),
        GaugeMetric.of(CONNECTIONS_ACTIVE).withTag(TAG_POOL)
    );

    private static final Map<MetricKey, Double> metricMap = new HashMap<>();

    private static class MetricKey {
        public final String metric;
        public final String tag;

        private MetricKey(final String metric, final String tag) {
            this.metric = metric;
            this.tag = tag;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final MetricKey metricKey = (MetricKey) o;
            return Objects.equals(metric, metricKey.metric) &&
                Objects.equals(tag, metricKey.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(metric, tag);
        }
    }

    public MetricWriterConfiguration(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 1000*60)
    @NoJobLogging
    void printMetrics() {
        final HashMap<MetricKey, Double> copyMetrics = new HashMap<>(metricMap);
        metricMap.clear();
        copyMetrics.keySet().forEach(this::logMeasurement);
    }

    @Scheduled(fixedRate = 50)
    @NoJobLogging
    void updateMetrics() {
        metricsToLog.forEach(this::updateMeasurement);
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
        final String tagValue = metric.tagName == null ? null : meter.getId().getTag(metric.tagName);
        final MetricVisitorData metricVisitorData = new MetricVisitorData(measurement, tagValue);

        metric.accept(this, metricVisitorData);
    }

    @Override
    public void visitCountMetric(final CountMetric countMetric, final MetricVisitorData metricVisitorData) {
        final String tagValue = metricVisitorData.tagValue();
        final Measurement measurement = metricVisitorData.measurement();

        final MetricKey metricKeyTotal = new MetricKey(countMetric.metricKey + ".total", tagValue);
        final MetricKey metricKeyPeriodic = new MetricKey(countMetric.metricKey + ".period", tagValue);

        final Double oldTotalValue = metricMap.get(metricKeyTotal);

        final Double newTotalValue = measurement.getValue();
        final Double newPeriodicValue = oldTotalValue == null ? newTotalValue : newTotalValue - oldTotalValue;

        metricMap.put(metricKeyTotal, newTotalValue);
        metricMap.put(metricKeyPeriodic, newPeriodicValue);
    }

    @Override
    public void visitGaugeMetric(final GaugeMetric gaugeMetric, final MetricVisitorData metricVisitorData) {
        final String tagValue = metricVisitorData.tagValue();
        final Measurement measurement = metricVisitorData.measurement();

        if(gaugeMetric.logMin) {
            final MetricKey metricKey = new MetricKey(gaugeMetric.metricKey + ".min", tagValue);

            final Double oldValue = metricMap.get(metricKey);
            final Double newValue = oldValue == null ? measurement.getValue() : Math.min(oldValue, measurement.getValue());

            metricMap.put(metricKey, newValue);
        }

        if(gaugeMetric.logMax) {
            final MetricKey metricKey = new MetricKey(gaugeMetric.metricKey + ".max", tagValue);

            final Double oldValue = metricMap.get(metricKey);
            final Double newValue = oldValue == null ? measurement.getValue() : Math.max(oldValue, measurement.getValue());

            metricMap.put(metricKey, newValue);
        }
    }

    private void logMeasurement(final MetricKey metricKey) {
        final Double value = metricMap.get(metricKey);

        if(value != null) {
            // must set root-locale to use . as decimal separator
            if(metricKey.tag != null) {
                LOG.info(String.format(Locale.ROOT, "meterName=%s statisticValue=%.02f tagName=%s", metricKey.metric, value, metricKey.tag));
            } else {
                LOG.info(String.format(Locale.ROOT, "meterName=%s statisticValue=%.02f", metricKey.metric, value));
            }
        }
    }
}
