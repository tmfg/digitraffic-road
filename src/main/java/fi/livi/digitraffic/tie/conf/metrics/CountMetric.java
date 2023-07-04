package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Statistic;

public class CountMetric extends  LoggableMetric {
    CountMetric(final String metricKey, final String tagName) {
        super(Statistic.COUNT, metricKey, tagName);
    }

    public CountMetric withTag(final String tagName) {
        return new CountMetric(metricKey, tagName);
    }

    public static CountMetric of(final String metricKey) {
        return new CountMetric(metricKey, null);
    }

    public void accept(MetricVisitor visitor, MetricVisitorData metricVisitorData) {
        visitor.visitCountMetric(this, metricVisitorData);
    }
}
