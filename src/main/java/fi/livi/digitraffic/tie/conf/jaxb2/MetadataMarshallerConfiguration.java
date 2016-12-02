package fi.livi.digitraffic.tie.conf.jaxb2;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraClient;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationClient;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationClient;

@Configuration
public class MetadataMarshallerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MetadataMarshallerConfiguration.class);
    public static final String NOT_CREATING_BEAN = "Not creating bean: ";

    @Bean
    public Jaxb2Marshaller marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
                "fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29",
                "fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06",
                "fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29",
                "fi.livi.ws.wsdl.lotju.metatiedot._2014._03._06",
                "fi.livi.ws.wsdl.lotju.metatiedot._2015._09._29",
                "fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29");
        return marshaller;
    }

    @Bean
    public LotjuTmsStationClient tmsStationClient(final Jaxb2Marshaller marshaller,
                                                  @Value("${metadata.server.address.tms}")
                                                  final String tmsMetadataServerAddress) {
        log.info("metadata.server.address.tms: " + tmsMetadataServerAddress);
        if ( StringUtils.isNotBlank(tmsMetadataServerAddress) &&
             !"${metadata.server.address.tms}".equals(tmsMetadataServerAddress) ) {
            log.info("Creating LotjuTmsStationClient");
            final LotjuTmsStationClient client = new LotjuTmsStationClient();
            client.setDefaultUri(tmsMetadataServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
            sender.setConnectionTimeout(30000);
            sender.setReadTimeout(30000);
            client.setMessageSender(sender);

            return client;
        }
        log.warn(NOT_CREATING_BEAN + LotjuTmsStationClient.class + " because property metadata.server.address.tms was not set.");
        return null;
    }

    @Bean
    public LotjuCameraClient cameraClient(final Jaxb2Marshaller marshaller,
                                          @Value("${metadata.server.address.camera}")
                                          final String cameraMetadataServerAddress) {

        log.info("metadata.server.address.camera: " + cameraMetadataServerAddress);
        if ( StringUtils.isNotBlank(cameraMetadataServerAddress) &&
             !"${metadata.server.address.camera}".equals(cameraMetadataServerAddress) ) {
            log.info("Creating LotjuCameraClient");
            final LotjuCameraClient client = new LotjuCameraClient();
            client.setDefaultUri(cameraMetadataServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
            sender.setConnectionTimeout(30000);
            sender.setReadTimeout(30000);
            client.setMessageSender(sender);

            return client;
        }
        log.warn(NOT_CREATING_BEAN + LotjuCameraClient.class + " because property metadata.server.address.camera was not set.");
        return null;
    }

    @Bean
    public LotjuWeatherStationClient weatherStationClient(final Jaxb2Marshaller marshaller,
                                                          @Value("${metadata.server.address.weather}")
                                                          final String weatherServerAddress) {

        log.info("metadata.server.address.weather: " + weatherServerAddress);
        if ( StringUtils.isNotBlank(weatherServerAddress) &&
                !"${metadata.server.address.weather}".equals(weatherServerAddress) ) {
            log.info("Creating LotjuWeatherStationClient");
            final LotjuWeatherStationClient client = new LotjuWeatherStationClient();
            client.setDefaultUri(weatherServerAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
            sender.setConnectionTimeout(30000);
            sender.setReadTimeout(30000);
            client.setMessageSender(sender);

            return client;
        }
        log.warn(NOT_CREATING_BEAN + LotjuWeatherStationClient.class + " because property metadata.server.address.weather was not set.");
        return null;
    }
}