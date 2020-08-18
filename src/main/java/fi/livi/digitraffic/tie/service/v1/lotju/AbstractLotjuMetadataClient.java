package fi.livi.digitraffic.tie.service.v1.lotju;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.destination.DestinationProvider;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import fi.livi.digitraffic.tie.service.IllegalArgumentException;

public class AbstractLotjuMetadataClient extends WebServiceGatewaySupport {

    /**
     *
     * @param marshaller Marshaller for SOAP messages
     * @param baseUrls ie. https://server1.com,https://server2.com
     * @param healthPath ie. /health
     * @param dataPath ie. /data/service
     * @param healthTtlSeconds How long is health status valid
     */
    public AbstractLotjuMetadataClient(final Jaxb2Marshaller marshaller, final String[] baseUrls, final String dataPath, final String healthPath,
                                       final int healthTtlSeconds) {
        setWebServiceTemplate(new WebServiceTemplateWithMultiDestinationProviderSupport());
        setDestinationProvider(new MultiDestinationProvider(HostWithHealthCheck.createHostsWithHealthCheck(baseUrls, dataPath, healthPath, healthTtlSeconds)));

        setMarshaller(marshaller);
        setUnmarshaller(marshaller);

        final HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
        sender.setConnectionTimeout(30000);
        sender.setReadTimeout(30000);
        setMessageSender(sender);

    }

    public static List<HostWithHealthCheck> createHostsWithHealthCheck(final String[] baseUrls, final String dataPath, final String healthPath, final int healthTtlSeconds) {
        if ( baseUrls == null || baseUrls.length == 0 ) {
            throw new IllegalArgumentException(String.format("method=createHostsWithHealthCheck failed because no addresses in baseUrls=%s:", baseUrls != null ? baseUrls.toString() : null));
        }
        return Arrays.stream(baseUrls)
            .map(baseUrl -> new HostWithHealthCheck(baseUrl, dataPath, healthPath, healthTtlSeconds))
            .collect(Collectors.toList());
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
