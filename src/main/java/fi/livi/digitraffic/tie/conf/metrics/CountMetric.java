package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Statistic;

public class CountMetric extends LoggableMetric {

    CountMetric(final String metricKey, final String tagName) {
        super(Statistic.COUNT, metricKey, tagName);
    }

    public static CountMetric of(final String metricKey) {
        return new CountMetric(metricKey, null);
    }

    @Override
    public CountMetric withTag(final String tagName) {
        return new CountMetric(metricKey, tagName);
    }

    @Override
    public void accept(final MetricVisitor visitor, final MetricVisitorData metricVisitorData) {
        visitor.visitCountMetric(this, metricVisitorData);
    }
}
