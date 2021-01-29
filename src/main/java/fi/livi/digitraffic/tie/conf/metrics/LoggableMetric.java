package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Statistic;

public class LoggableMetric {
    public final String metricKey;
    public final Statistic statistic;

    public LoggableMetric(final String metricKey) {
        this.metricKey = metricKey;
        this.statistic = Statistic.VALUE;
    }

    public String loggingKey(final Meter meter) {
        return metricKey;
    }
}
