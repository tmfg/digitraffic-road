package fi.livi.digitraffic.tie.conf.metrics;

public interface MetricVisitor {
    void visitCountMetric(CountMetric countMetric, MetricVisitorData metricVisitorData);
    void visitGaugeMetric(GaugeMetric gaugeMetric, MetricVisitorData metricVisitorData);
}
