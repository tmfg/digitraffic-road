package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Meter;

public class TaggableMetric extends LoggableMetric {
    private final String metricKeyStart;
    private final String tagName;
    private final String metricKeyEnd;

    public TaggableMetric(final String metricKeyStart, final String metricKeyEnd, final String tagName) {
        super(metricKeyStart + "." + metricKeyEnd);
        this.metricKeyStart = metricKeyStart;
        this.metricKeyEnd = metricKeyEnd;
        this.tagName = tagName;
    }

    @Override
    public String loggingKey(final Meter meter) {
        final String tagValue = meter.getId().getTag(tagName);
        return metricKeyStart + "." + tagValue + "." + metricKeyEnd;
    }
}
