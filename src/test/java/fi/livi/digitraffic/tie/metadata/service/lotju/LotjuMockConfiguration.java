package fi.livi.digitraffic.tie.metadata.service.lotju;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@ConditionalOnNotWebApplication
@Configuration
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
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuKameraPerustiedotServiceEndpointMock.getInstance(lotjuCameraStationMetadataClient.getServerAddress(), resourceLoader, jaxb2Marshaller);
    }

    @Bean
    public LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuLAMMetatiedotServiceEndpointMock.getInstance(lotjuTmsStationMetadataClient.getServerAddress(), resourceLoader, jaxb2Marshaller);
    }

    @Bean
    public LotjuTiesaaPerustiedotServiceEndpointMock lotjuTiesaaPerustiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuTiesaaPerustiedotServiceEndpointMock.getInstance(lotjuWeatherStationMetadataClient.getServerAddress(), resourceLoader, jaxb2Marshaller);
    }

}
