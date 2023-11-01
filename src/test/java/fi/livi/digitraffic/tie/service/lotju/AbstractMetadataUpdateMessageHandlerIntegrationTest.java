package fi.livi.digitraffic.tie.service.lotju;

import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTest;

public abstract class AbstractMetadataUpdateMessageHandlerIntegrationTest extends AbstractDaemonTest {
    @MockBean
    protected LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;

    @MockBean
    protected LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

}
