package fi.livi.digitraffic.tie.metadata.service.lotju;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class LotjuConfiguration {

    @Bean
    public LotjuKameraPerustiedotServiceEndpoint lotjuKameraPerustiedotServiceEndpoint(
            @Value("${metadata.server.address.camera}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuKameraPerustiedotServiceEndpoint.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

    @Bean
    public LotjuLAMMetatiedotServiceEndpoint lotjuLAMMetatiedotServiceEndpoint(
            @Value("${metadata.server.address.tms}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuLAMMetatiedotServiceEndpoint.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

    @Bean
    public LotjuTiesaaPerustiedotServiceEndpoint lotjuTiesaaPerustiedotServiceEndpoint(
            @Value("${metadata.server.address.weather}")
            final String metadataServerAddress,
            final ResourceLoader resourceLoader,
            final Jaxb2Marshaller jaxb2Marshaller) {
        return LotjuTiesaaPerustiedotServiceEndpoint.getInstance(metadataServerAddress, resourceLoader, jaxb2Marshaller);
    }

}
