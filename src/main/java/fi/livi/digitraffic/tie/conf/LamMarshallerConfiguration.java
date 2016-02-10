package fi.livi.digitraffic.tie.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.service.lam.LamStationClient;

@Configuration
public class LamMarshallerConfiguration {
    @Bean
    public Jaxb2Marshaller marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPath("fi.livi.digitraffic.tie.wsdl.lam");

        return marshaller;
    }

    @Bean
    public LamStationClient lamStationClient(final Jaxb2Marshaller marshaller, @Value("${metadata.server.address}") final String metadataServerAddress) {
        final LamStationClient client = new LamStationClient();

        client.setAddress(metadataServerAddress);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);

        return client;
    }
}