package fi.livi.digitraffic.tie.metadata.service.lotju;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class LotjuMockConfiguration {

    @Bean
    public LotjuKameraPerustiedotServiceEndpointMock lotjuKameraPerustiedotServiceEndpoint(
            @Value("${metadata.server.address.camera}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuKameraPerustiedotServiceEndpointMock.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

    @Bean
    public LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceEndpoint(
            @Value("${metadata.server.address.tms}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuLAMMetatiedotServiceEndpointMock.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

    @Bean
    public LotjuTiesaaPerustiedotServiceEndpointMock lotjuTiesaaPerustiedotServiceEndpoint(
            @Value("${metadata.server.address.weather}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuTiesaaPerustiedotServiceEndpointMock.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

}
