package fi.livi.digitraffic.tie.metadata.service.lotju;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

public class AbstractLotjuMetadataClient extends WebServiceGatewaySupport {

    private final boolean enabled;

    public AbstractLotjuMetadataClient(Jaxb2Marshaller marshaller, String serverAddress, Logger log) {

        if ( StringUtils.isNotBlank(serverAddress) &&
             StringUtils.containsNone(serverAddress, '$', '{', '}') ) {
            log.info("Init {} with server address {}", getClass().getSimpleName(), serverAddress);
            setDefaultUri(serverAddress);
            setMarshaller(marshaller);
            setUnmarshaller(marshaller);

            HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
            sender.setConnectionTimeout(30000);
            sender.setReadTimeout(30000);
            setMessageSender(sender);
            enabled = true;
        } else {
            log.warn("Not setting up bean: {} because server address ({}) property was not set.", getClass().getSimpleName(), serverAddress);
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
