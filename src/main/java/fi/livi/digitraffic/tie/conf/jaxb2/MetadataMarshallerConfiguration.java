package fi.livi.digitraffic.tie.conf.jaxb2;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraClient;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationClient;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationClient;

@Configuration
public class MetadataMarshallerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MetadataMarshallerConfiguration.class);

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

        return initClientPropertiesOrNull(new LotjuTmsStationClient(), tmsMetadataServerAddress, marshaller);
    }

    @Bean
    public LotjuCameraClient cameraClient(final Jaxb2Marshaller marshaller,
                                          @Value("${metadata.server.address.camera}")
                                          final String cameraMetadataServerAddress) {

        return initClientPropertiesOrNull(new LotjuCameraClient(), cameraMetadataServerAddress, marshaller);
    }

    @Bean
    public LotjuWeatherStationClient weatherStationClient(final Jaxb2Marshaller marshaller,
                                                          @Value("${metadata.server.address.weather}")
                                                          final String weatherServerAddress) {

        return initClientPropertiesOrNull(new LotjuWeatherStationClient(), weatherServerAddress, marshaller);
    }

    /**
     * Returns client if server address is ok otherwise returns null
     */
    private <T extends WebServiceGatewaySupport> T initClientPropertiesOrNull(final T client,
                                                                              final String serverAddress,
                                                                              final Jaxb2Marshaller marshaller) {
        log.info("Init {} with server address {}", client.getClass().getSimpleName(), serverAddress);

        if ( StringUtils.isNotBlank(serverAddress) &&
             StringUtils.containsNone(serverAddress, '$', '{', '}') ) {
            log.info("Creating " + client.getClass().getSimpleName());
            client.setDefaultUri(serverAddress);
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);

            HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
            sender.setConnectionTimeout(30000);
            sender.setReadTimeout(30000);
            client.setMessageSender(sender);
            return client;
        }
        log.warn("Not creating bean: {} because server property was not set.", client.getClass().getSimpleName());
        return null;
    }
}