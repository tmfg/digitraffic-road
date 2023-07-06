package fi.livi.digitraffic.tie.conf.metrics;

public interface MetricVisitor {
    public void visitCountMetric(CountMetric countMetric, MetricVisitorData metricVisitorData);
    public void visitGaugeMetric(GaugeMetric gaugeMetric, MetricVisitorData metricVisitorData);
}
