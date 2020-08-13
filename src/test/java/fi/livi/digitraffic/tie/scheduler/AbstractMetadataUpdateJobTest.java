package fi.livi.digitraffic.tie.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.client.support.destination.DestinationProvider;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.service.v1.lotju.AbstractLotjuMetadataClient;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuCameraStationMetadataClient;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataClient;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuWeatherStationMetadataClient;
import fi.livi.digitraffic.tie.service.v1.lotju.MultiDestinationProvider;

public class AbstractMetadataUpdateJobTest extends AbstractDaemonTestWithoutS3 {

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
