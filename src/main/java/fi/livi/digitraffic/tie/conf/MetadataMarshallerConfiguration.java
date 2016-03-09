package fi.livi.digitraffic.tie.conf;

import fi.livi.digitraffic.tie.service.camera.CameraClient;
import fi.livi.digitraffic.tie.service.lam.LamStationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class MetadataMarshallerConfiguration {

    @Bean
    public Jaxb2Marshaller marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
                "fi.livi.digitraffic.tie.wsdl.lam",
                "fi.livi.digitraffic.tie.wsdl.kamera",
                "fi.livi.digitraffic.tie.wsdl.tiesaa");
        return marshaller;
    }

    @Bean
    public LamStationClient lamStationClient(final Jaxb2Marshaller marshaller,
                                             @Value("${metadata.server.address.lam}")
                                             final String metadataServerAddress) {

        final LamStationClient client = new LamStationClient();
        client.setAddress(metadataServerAddress);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);

        return client;
    }

    @Bean
    public CameraClient cameraClient(final Jaxb2Marshaller marshaller,
                                     @Value("${metadata.server.address.camera}")
                                     final String cameraMetadataServerAddress) {

        final CameraClient client = new CameraClient();
        client.setAddress(cameraMetadataServerAddress);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);

        return client;
    }
}