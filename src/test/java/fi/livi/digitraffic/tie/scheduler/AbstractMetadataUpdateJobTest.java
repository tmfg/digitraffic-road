package fi.livi.digitraffic.tie.scheduler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.support.destination.DestinationProvider;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.service.v1.lotju.AbstractLotjuMetadataClient;
import fi.livi.digitraffic.tie.service.v1.lotju.MultiDestinationProvider;

public abstract class AbstractMetadataUpdateJobTest extends AbstractDaemonTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractMetadataUpdateJobTest.class);

    private Map<AbstractLotjuMetadataClient, DestinationProvider> lotjuClienOriginalDestinationProvider = new HashMap<>();

    public void setLotjuClientFirstDestinationProviderAndSaveOroginalToMap(final AbstractLotjuMetadataClient lotjuClient) {
        final AbstractLotjuMetadataClient tgt = getTargetObject(lotjuClient);
        final DestinationProvider original = tgt.getDestinationProvider();
        lotjuClienOriginalDestinationProvider.put(tgt, original);

        final URI firstDest = ((MultiDestinationProvider) original).getDestinations().get(0);
        log.info("Set DestinationProvider url to first destination {} for {}", firstDest, tgt.getClass());
        tgt.setDestinationProvider(() -> firstDest);
    }

    public void restoreLotjuClientDestinationProvider(final AbstractLotjuMetadataClient lotjuClient) {
        final AbstractLotjuMetadataClient tgt = getTargetObject(lotjuClient);
        final DestinationProvider originalDP = lotjuClienOriginalDestinationProvider.get(tgt);
        tgt.setDestinationProvider(originalDP);
    }
}
