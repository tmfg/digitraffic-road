package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Statistic;

abstract public class LoggableMetric {
    public final String metricKey;
    public final Statistic statistic;
    public final String tagName;

    protected LoggableMetric(final Statistic statistic, final String metricKey, final String tagName) {
        this.metricKey = metricKey;
        this.tagName = tagName;
        this.statistic = statistic;
    }

    protected LoggableMetric(final Statistic statistic, final String metricKey) {
        this.metricKey = metricKey;
        this.tagName = null;
        this.statistic = statistic;
    }

    abstract public LoggableMetric withTag(final String tagName);

    abstract public void accept(MetricVisitor visitor, MetricVisitorData metricVisitorData);
}
