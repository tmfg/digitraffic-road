package fi.livi.digitraffic.tie.service.lotju;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.service.tms.TmsSensorUpdater;
import fi.livi.digitraffic.tie.service.tms.TmsStationSensorConstantUpdater;
import fi.livi.digitraffic.tie.service.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.service.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.service.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.service.weathercam.CameraStationUpdater;

public abstract class AbstractMetadataUpdateMessageHandlerTest extends AbstractDaemonTest {

    @MockitoBean
    protected TmsStationUpdater tmsStationUpdater;

    @MockitoBean
    protected TmsSensorUpdater tmsSensorUpdater;

    @MockitoBean
    protected TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater;

    @MockitoBean
    protected WeatherStationUpdater weatherStationUpdater;

    @MockitoBean
    protected WeatherStationSensorUpdater weatherStationSensorUpdater;

    @MockitoBean
    protected CameraStationUpdater cameraStationUpdater;

}
