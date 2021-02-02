package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Statistic;

public class LoggableMetric {
    public final String metricKey;
    public final Statistic statistic;
    public final String tagName;

    public final boolean logMin;
    public final boolean logMax;

    protected LoggableMetric(final String metricKey, final String tagName, final boolean logMin, final boolean logMax) {
        this.metricKey = metricKey;
        this.tagName = tagName;
        this.statistic = Statistic.VALUE;
        this.logMin = logMin;
        this.logMax = logMax;
    }

    public static LoggableMetric of(final String metricKey) {
        return new LoggableMetric(metricKey, null, true, true);
    }

    public LoggableMetric noMin() {
        return new LoggableMetric(metricKey, tagName, false, logMax);
    }

    public LoggableMetric withTag(final String tagName) {
        return new LoggableMetric(metricKey, tagName, logMin, logMax);
    }
}
