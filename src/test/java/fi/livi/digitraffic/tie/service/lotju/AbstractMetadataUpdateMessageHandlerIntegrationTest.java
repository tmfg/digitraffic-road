package fi.livi.digitraffic.tie.service.lotju;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import fi.livi.digitraffic.tie.AbstractDaemonTest;

public abstract class AbstractMetadataUpdateMessageHandlerIntegrationTest extends AbstractDaemonTest {
    @MockitoBean
    protected LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;

    @MockitoBean
    protected LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

}
