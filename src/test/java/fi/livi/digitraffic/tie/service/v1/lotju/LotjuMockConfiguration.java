package fi.livi.digitraffic.tie.service.v1.lotju;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

//@ConditionalOnNotWebApplication
//@Configuration
public class LotjuMockConfiguration {

    @Autowired
    LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @Autowired
    LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @Autowired
    LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;

    @Bean
    public LotjuKameraPerustiedotServiceEndpointMock lotjuKameraPerustiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            @Qualifier("kameraMetadataJaxb2Marshaller")
            final Jaxb2Marshaller kameraMetadataJaxb2Marshaller,
            @Value("${metadata.server.addresses}") final String serverAddresses,
            @Value("${metadata.server.path.camera}") final String dataPath) {
        final String srv = StringUtils.split(serverAddresses, ",")[0];
        return LotjuKameraPerustiedotServiceEndpointMock.getInstance(srv + dataPath, resourceLoader, kameraMetadataJaxb2Marshaller);
    }

    @Bean
    public LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            @Qualifier("lamMetadataJaxb2Marshaller")
            final Jaxb2Marshaller lamMetadataJaxb2Marshaller,
            @Value("${metadata.server.addresses}") final String serverAddresses,
            @Value("${metadata.server.path.tms}") final String dataPath) {
        final String srv = StringUtils.split(serverAddresses, ",")[0];
        return LotjuLAMMetatiedotServiceEndpointMock.getInstance(srv + dataPath, resourceLoader, lamMetadataJaxb2Marshaller);
    }

    @Bean
    public LotjuTiesaaPerustiedotServiceEndpointMock lotjuTiesaaPerustiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            @Qualifier("tiesaaMetadataJaxb2Marshaller")
            final Jaxb2Marshaller tiesaaMetadataJaxb2Marshaller,
            @Value("${metadata.server.addresses}") final String serverAddresses,
            @Value("${metadata.server.path.weather}") final String dataPath) {
        final String srv = StringUtils.split(serverAddresses, ",")[0];
        return LotjuTiesaaPerustiedotServiceEndpointMock.getInstance(srv + dataPath, resourceLoader, tiesaaMetadataJaxb2Marshaller);
    }

}
