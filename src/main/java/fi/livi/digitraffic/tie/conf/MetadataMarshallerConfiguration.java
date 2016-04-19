package fi.livi.digitraffic.tie.conf;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.metadata.service.camera.CameraClient;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationClient;

@Configuration
public class MetadataMarshallerConfiguration {

    private static final Logger LOG = Logger.getLogger(MetadataMarshallerConfiguration.class);
    public static final String NOT_CREATING_BEAN = "Not creating bean: ";

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
                                             final String lamMetadataServerAddress) {
        LOG.info("metadata.server.address.lam: " + lamMetadataServerAddress);
        if ( StringUtils.isNotBlank(lamMetadataServerAddress) &&
             !"${metadata.server.address.lam}".equals(lamMetadataServerAddress) ) {
            LOG.info("Creating LamStationClient");
            final LamStationClient client = new LamStationClient();
            client.setAddress(lamMetadataServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            return client;
        }
        LOG.warn(NOT_CREATING_BEAN + LamStationClient.class + " because property metadata.server.address.lam was not set.");
        return null;
    }

    @Bean
    public CameraClient cameraClient(final Jaxb2Marshaller marshaller,
                                     @Value("${metadata.server.address.camera}")
                                     final String cameraMetadataServerAddress) {

        LOG.info("metadata.server.address.camera: " + cameraMetadataServerAddress);
        if ( StringUtils.isNotBlank(cameraMetadataServerAddress) &&
             !"${metadata.server.address.camera}".equals(cameraMetadataServerAddress) ) {
            LOG.info("Creating CameraClient");
            final CameraClient client = new CameraClient();
            client.setAddress(cameraMetadataServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            return client;
        }
        LOG.warn(NOT_CREATING_BEAN + CameraClient.class + " because property metadata.server.address.camera was not set.");
        return null;
    }

    @Bean
    public RoadWeatherStationClient roadWeatherStationClient(final Jaxb2Marshaller marshaller,
                                                             @Value("${metadata.server.address.weather}")
                                                             final String roadWeatherServerAddress) {

        LOG.info("metadata.server.address.weather: " + roadWeatherServerAddress);
        if ( StringUtils.isNotBlank(roadWeatherServerAddress) &&
                !"${metadata.server.address.weather}".equals(roadWeatherServerAddress) ) {
            LOG.info("Creating RoadWeatherStationClient");
            final RoadWeatherStationClient client = new RoadWeatherStationClient();
            client.setAddress(roadWeatherServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            return client;
        }
        LOG.warn(NOT_CREATING_BEAN + RoadWeatherStationClient.class + " because property metadata.server.address.weather was not set.");
        return null;
    }
}