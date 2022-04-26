package fi.livi.digitraffic.tie.service.v1;

import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.service.v1.camera.CameraStationUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsSensorUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorConstantUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationUpdater;

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
