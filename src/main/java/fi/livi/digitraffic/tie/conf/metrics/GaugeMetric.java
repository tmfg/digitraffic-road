package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Statistic;

public class GaugeMetric extends LoggableMetric {

    public final boolean logMin;
    public final boolean logMax;

    GaugeMetric(final String metricKey, final String tagName, final boolean logMin, final boolean logMax) {
        super(Statistic.VALUE, metricKey, tagName);
        this.logMin = logMin;
        this.logMax = logMax;
    }

    public static GaugeMetric of(final String metricKey) {
        return new GaugeMetric(metricKey, null, true, true);
    }

    @Override
    public GaugeMetric withTag(final String tagName) {
        return new GaugeMetric(metricKey, tagName, logMin, logMax);
    }

    @Override
    public void accept(MetricVisitor visitor, MetricVisitorData metricVisitorData) {
        visitor.visitGaugeMetric(this, metricVisitorData);
    }

    public LoggableMetric noMin() {
        return new GaugeMetric(metricKey, tagName, false, logMax);
    }
}
