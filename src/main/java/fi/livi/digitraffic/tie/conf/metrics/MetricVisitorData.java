package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Measurement;

public record MetricVisitorData(Measurement measurement, String tagValue) {
}
