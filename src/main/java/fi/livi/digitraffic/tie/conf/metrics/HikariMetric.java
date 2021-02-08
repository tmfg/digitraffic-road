package fi.livi.digitraffic.tie.conf.metrics;

public class HikariMetric extends TaggableMetric {
    private static final String HIKARI_METRIC_ID_START = "hikaricp.connections";

    public HikariMetric(final String hikariMetric) {
        super(HIKARI_METRIC_ID_START, hikariMetric, "pool");
    }
}
