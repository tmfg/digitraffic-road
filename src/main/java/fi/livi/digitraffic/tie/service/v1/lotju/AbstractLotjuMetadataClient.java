package fi.livi.digitraffic.tie.service.v1.lotju;

import java.net.URI;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.destination.DestinationProvider;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

public class AbstractLotjuMetadataClient extends WebServiceGatewaySupport {

    public AbstractLotjuMetadataClient(final Jaxb2Marshaller marshaller, final String serverAddresses, final String healthPath, final String dataPath,
                                       final int healthTtlSeconds, final Logger log) {
        final String[] addresses = StringUtils.split(serverAddresses, ",");
        if (addresses == null || addresses.length == 0) {
            throw new IllegalArgumentException(String.format("Failed to set upt beanName=%s with empty serverAddresses=%s", getClass().getSimpleName(), serverAddresses));
        }
        setWebServiceTemplate(new WebServiceTemplateWithMultiDestinationProviderSupport());
        setDestinationProvider(new MultiDestinationProvider(serverAddresses, healthPath, dataPath, healthTtlSeconds));

        setMarshaller(marshaller);
        setUnmarshaller(marshaller);

        final HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
        sender.setConnectionTimeout(30000);
        sender.setReadTimeout(30000);
        setMessageSender(sender);

    }

    protected Object marshalSendAndReceive(final JAXBElement<?> requestPayload) {
        return getWebServiceTemplate().marshalSendAndReceive(requestPayload);
    }

    public static class WebServiceTemplateWithMultiDestinationProviderSupport extends WebServiceTemplate {

        private static final Logger log = LoggerFactory.getLogger(WebServiceTemplateWithMultiDestinationProviderSupport.class);

        @Override
        public Object marshalSendAndReceive(final Object requestPayload, final WebServiceMessageCallback requestCallback) {
            final DestinationProvider dp = getDestinationProvider();

            if ( dp instanceof MultiDestinationProvider ) {
                final MultiDestinationProvider mdp = (MultiDestinationProvider) dp;
                int tryCount = 0;

                Exception lastException;
                do {
                    tryCount++;
                    final URI dest = mdp.getDestination();
                    String dataUri = null;
                    try {
                        dataUri = getDefaultUri();
                        final Object value = marshalSendAndReceive(dataUri, requestPayload, requestCallback);
                        // mark host as healthy
                        mdp.setHostHealthy(dest);
                        return value;
                    } catch (Exception e) {
                        // mark host not healthy
                        mdp.setHostNotHealthy(dest);
                        lastException = e;
                        log.error(String.format("method=marshalSendAndReceive returned error for dataUrl=%s", dataUri), lastException);
                    }
                } while (tryCount < mdp.getDestinationsCount());
                throw new IllegalStateException(String.format("No host found to return data without error dataUrls=%s", mdp.getDestinationsAsString()),
                                                lastException);
            }

            return marshalSendAndReceive(getDefaultUri(), requestPayload, requestCallback);
        }
    }
}
