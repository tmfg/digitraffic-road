package fi.livi.digitraffic.tie.conf.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.conf.amazon.RoadForecastS3Properties;

@Configuration
@EnableConfigurationProperties({ LotjuMetadataProperties.class, RoadForecastS3Properties.class })
public class PropertiesConfiguration {
}
