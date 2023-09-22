package fi.livi.digitraffic.tie;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.conf.amazon.AmazonS3ClientTestConfiguration;
import fi.livi.digitraffic.tie.conf.amazon.S3PropertiesConfiguration;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.conf.properties.PropertiesConfiguration;
import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.dao.LockingDao;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryDao;
import fi.livi.digitraffic.tie.dao.v1.RoadStationDao;
import fi.livi.digitraffic.tie.dao.v1.SensorValueDao;
import fi.livi.digitraffic.tie.dao.v1.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.helper.FileHttpGetClient;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.service.*;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingMqttDataService;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingUpdateServiceV1;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsDataWebServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.ImsJsonConverter;
import fi.livi.digitraffic.tie.service.trafficmessage.V2Datex2JsonConverter;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.location.LocationWebServiceV1;
import fi.livi.digitraffic.tie.service.v1.SensorDataUpdateService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.camera.*;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.v1.location.*;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationService;
import fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryGitClient;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Import({// configurations
         AmazonS3ClientTestConfiguration.class, S3PropertiesConfiguration.class, PropertiesConfiguration.class, JacksonAutoConfiguration.class,
         Datex2XmlStringToObjectMarshaller.class, XmlMarshallerConfiguration.class, RestTemplate.class, RetryTemplate.class,

         // services
         LocationService.class, CameraPresetService.class, TmsStationService.class, DataStatusService.class,
         RoadStationService.class, TmsStationSensorConstantService.class, RoadStationSensorService.class,
         CameraImageUpdateHandler.class, CameraImageReader.class, CameraImageS3Writer.class, FileHttpGetClient.class,
         CameraPresetHistoryUpdateService.class, FlywayService.class,
         WeatherService.class, SensorDataUpdateService.class,
         ImsJsonConverter.class, V2Datex2UpdateService.class,
         V2Datex2JsonConverter.class,
         V3RegionGeometryUpdateService.class,
         MaintenanceTrackingUpdateServiceV1.class,
         LocationTypeUpdater.class, LocationMetadataUpdater.class, LocationUpdater.class, LocationSubtypeUpdater.class,
         MetadataFileFetcher.class, ClusteredLocker.class, LockingServiceInternal.class,
         V3RegionGeometryDataService.class,

         // V1 services
         RoadStationSensorServiceV1.class, TmsDataWebServiceV1.class, TmsStationMetadataWebServiceV1.class,
         MaintenanceTrackingMqttDataService.class, LocationWebServiceV1.class,

         // converters
         CoordinateConverter.class, StationSensorConverterService.class,
         ObjectMapper.class,

         // daos
         TmsSensorConstantDao.class, SensorValueDao.class, RoadStationDao.class, SensorValueHistoryDao.class,
         LockingDao.class,

         // Test services etc.
         TrafficMessageTestHelper.class, MaintenanceTrackingServiceTestHelperV1.class, TmsTestHelper.class
})
public abstract class AbstractServiceTest extends AbstractJpaTest {

    @MockBean
    protected RegionGeometryGitClient regionGeometryGitClientMock;

    @SpyBean
    protected MetadataFileFetcher metadataFileFetcherSpy;

}
