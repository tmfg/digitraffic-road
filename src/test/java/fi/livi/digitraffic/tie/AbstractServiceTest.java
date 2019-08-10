package fi.livi.digitraffic.tie;

import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.conf.jaxb2.MetadataMarshallerConfiguration;
import fi.livi.digitraffic.tie.data.service.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.data.service.TmsDataService;
import fi.livi.digitraffic.tie.data.service.datex2.StringToObjectMarshaller;
import fi.livi.digitraffic.tie.metadata.converter.StationSensorConverter;
import fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.RoadDistrictService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.location.LocationService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

@Import({StringToObjectMarshaller.class, MetadataMarshallerConfiguration.class,
    // services
    LocationService.class, RoadDistrictService.class, CameraPresetService.class, TmsStationService.class, DataStatusService.class,
    RoadStationService.class, FreeFlowSpeedService.class, TmsStationSensorConstantService.class, RoadStationSensorService.class,
    TmsDataService.class,

    // converters
    TmsStationMetadata2FeatureConverter.class, CoordinateConverter.class, StationSensorConverter.class,

    // daos
    TmsSensorConstantDao.class
})
public abstract class AbstractServiceTest extends AbstractJpaTest {
}
