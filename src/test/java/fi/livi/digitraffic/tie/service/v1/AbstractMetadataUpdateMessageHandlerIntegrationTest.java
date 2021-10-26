package fi.livi.digitraffic.tie.service.v1;

import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataClient;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuWeatherStationMetadataClient;

public abstract class AbstractMetadataUpdateMessageHandlerIntegrationTest extends AbstractDaemonTest {
    @MockBean
    protected LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;

    @MockBean
    protected LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

}
