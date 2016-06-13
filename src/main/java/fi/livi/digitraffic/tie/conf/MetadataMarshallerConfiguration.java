package fi.livi.digitraffic.tie.conf;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraClient;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuLamStationClient;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuRoadWeatherStationClient;

@Configuration
public class MetadataMarshallerConfiguration {

    private static final Logger LOG = Logger.getLogger(MetadataMarshallerConfiguration.class);
    public static final String NOT_CREATING_BEAN = "Not creating bean: ";

    @Bean
    public Jaxb2Marshaller marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
                "fi.livi.digitraffic.tie.lotju.wsdl.lam",
                "fi.livi.digitraffic.tie.lotju.wsdl.kamera",
                "fi.livi.digitraffic.tie.lotju.wsdl.tiesaa");
        return marshaller;
    }

    @Bean
    public LotjuLamStationClient lamStationClient(final Jaxb2Marshaller marshaller,
                                                  @Value("${metadata.server.address.lam}")
                                             final String lamMetadataServerAddress) {
        LOG.info("metadata.server.address.lam: " + lamMetadataServerAddress);
        if ( StringUtils.isNotBlank(lamMetadataServerAddress) &&
             !"${metadata.server.address.lam}".equals(lamMetadataServerAddress) ) {
            LOG.info("Creating LotjuLamStationClient");
            final LotjuLamStationClient client = new LotjuLamStationClient();
            client.setAddress(lamMetadataServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            return client;
        }
        LOG.warn(NOT_CREATING_BEAN + LotjuLamStationClient.class + " because property metadata.server.address.lam was not set.");
        return null;
    }

    @Bean
    public LotjuCameraClient cameraClient(final Jaxb2Marshaller marshaller,
                                          @Value("${metadata.server.address.camera}")
                                     final String cameraMetadataServerAddress) {

        LOG.info("metadata.server.address.camera: " + cameraMetadataServerAddress);
        if ( StringUtils.isNotBlank(cameraMetadataServerAddress) &&
             !"${metadata.server.address.camera}".equals(cameraMetadataServerAddress) ) {
            LOG.info("Creating LotjuCameraClient");
            final LotjuCameraClient client = new LotjuCameraClient();
            client.setAddress(cameraMetadataServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            return client;
        }
        LOG.warn(NOT_CREATING_BEAN + LotjuCameraClient.class + " because property metadata.server.address.camera was not set.");
        return null;
    }

    @Bean
    public LotjuRoadWeatherStationClient roadWeatherStationClient(final Jaxb2Marshaller marshaller,
                                                                  @Value("${metadata.server.address.weather}")
                                                             final String roadWeatherServerAddress) {

        LOG.info("metadata.server.address.weather: " + roadWeatherServerAddress);
        if ( StringUtils.isNotBlank(roadWeatherServerAddress) &&
                !"${metadata.server.address.weather}".equals(roadWeatherServerAddress) ) {
            LOG.info("Creating LotjuRoadWeatherStationClient");
            final LotjuRoadWeatherStationClient client = new LotjuRoadWeatherStationClient();
            client.setAddress(roadWeatherServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            return client;
        }
        LOG.warn(NOT_CREATING_BEAN + LotjuRoadWeatherStationClient.class + " because property metadata.server.address.weather was not set.");
        return null;
    }
}