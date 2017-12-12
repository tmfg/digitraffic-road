package fi.livi.digitraffic.tie.metadata.service.lotju;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class LotjuConfiguration {

    @Bean
    public LotjuKameraPerustiedotServiceMockEndpoint lotjuKameraPerustiedotServiceEndpoint(
            @Value("${metadata.server.address.camera}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuKameraPerustiedotServiceMockEndpoint.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

    @Bean
    public LotjuLAMMetatiedotServiceMockEndpoint lotjuLAMMetatiedotServiceEndpoint(
            @Value("${metadata.server.address.tms}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuLAMMetatiedotServiceMockEndpoint.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

    @Bean
    public LotjuTiesaaPerustiedotServiceMockEndpoint lotjuTiesaaPerustiedotServiceEndpoint(
            @Value("${metadata.server.address.weather}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuTiesaaPerustiedotServiceMockEndpoint.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

}
