package fi.livi.digitraffic.tie.conf.amazon;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "dt.amazon.s3.roadforecast")
public class RoadForecastS3Properties extends S3Properties {
    private final String prefix;
    private final Duration window;

    public RoadForecastS3Properties(@DefaultValue("") final String bucketName,
                                    final String region,
                                    @DefaultValue("forecast/") final String prefix,
                                    @DefaultValue("24") final int windowHours) {
        super(bucketName);

        setS3Region(region);
        this.prefix = prefix;
        this.window = Duration.ofHours(windowHours);
    }

    public String getPrefix() {
        return prefix;
    }

    /** How far back from now the latest forecast file is searched */
    public Duration getWindow() {
        return window;
    }
}
