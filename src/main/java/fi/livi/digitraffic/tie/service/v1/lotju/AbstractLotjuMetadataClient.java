package fi.livi.digitraffic.tie.service.v1.lotju;

import java.net.URI;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.destination.DestinationProvider;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

public abstract class AbstractLotjuMetadataClient extends WebServiceGatewaySupport {

    /**
     *
     * @param marshaller Marshaller for SOAP messages
     * @param baseUrls ie. https://server1.com,https://server2.com
     * @param healthPath ie. /health
     * @param dataPath ie. /data/service
     * @param healthTtlSeconds How long is health status valid
     */
    public AbstractLotjuMetadataClient(final Jaxb2Marshaller marshaller, final String[] baseUrls, final String dataPath, final String healthPath,
                                       final int healthTtlSeconds, final String healthOkValue) {
        setWebServiceTemplate(new WebServiceTemplateWithMultiDestinationProviderSupport());
        setDestinationProvider(new MultiDestinationProvider(HostWithHealthCheck.createHostsWithHealthCheck(baseUrls, dataPath, healthPath, healthTtlSeconds, healthOkValue)));

        setMarshaller(marshaller);
        setUnmarshaller(marshaller);

        final HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
        sender.setConnectionTimeout(60000);
        sender.setReadTimeout(60000);
        setMessageSender(sender);

    }

    public AbstractLotjuMetadataClient(final Jaxb2Marshaller marshaller, final String[] baseUrls, final String dataPath, final int healthTtlSeconds) {
        this(marshaller, baseUrls, dataPath, null, healthTtlSeconds, null);
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
                        log.warn(String.format("method=marshalSendAndReceive returned error for dataUrl=%s", dataUri), lastException);
                    }
                } while (tryCount < mdp.getDestinationsCount());
                throw new IllegalStateException(String.format("No host found to return data without error dataUrls=%s", mdp.getDestinationsAsString()),
                                                lastException);
            }

            return marshalSendAndReceive(getDefaultUri(), requestPayload, requestCallback);
        }
    }
}
