package fi.livi.digitraffic.tie.service.lotju;

import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.service.tms.TmsSensorUpdater;
import fi.livi.digitraffic.tie.service.tms.TmsStationSensorConstantUpdater;
import fi.livi.digitraffic.tie.service.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.service.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.service.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.service.weathercam.CameraStationUpdater;

public abstract class AbstractMetadataUpdateMessageHandlerTest extends AbstractDaemonTest {

    @MockBean
    protected TmsStationUpdater tmsStationUpdater;

    @MockBean
    protected TmsSensorUpdater tmsSensorUpdater;

    @MockBean
    protected TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater;

    @MockBean
    protected WeatherStationUpdater weatherStationUpdater;

    @MockBean
    protected WeatherStationSensorUpdater weatherStationSensorUpdater;

    @MockBean
    protected CameraStationUpdater cameraStationUpdater;

}
