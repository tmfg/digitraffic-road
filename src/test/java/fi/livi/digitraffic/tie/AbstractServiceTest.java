package fi.livi.digitraffic.tie;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.conf.amazon.AmazonS3ClientTestConfiguration;
import fi.livi.digitraffic.tie.conf.amazon.S3PropertiesConfiguration;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.conf.properties.PropertiesConfiguration;
import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.converter.feature.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.dao.LockingDao;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryDao;
import fi.livi.digitraffic.tie.dao.v1.RoadStationDao;
import fi.livi.digitraffic.tie.dao.v1.SensorValueDao;
import fi.livi.digitraffic.tie.dao.v1.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.helper.FileHttpGetClient;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.FlywayService;
import fi.livi.digitraffic.tie.service.LockingServiceInternal;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.TmsTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsDataWebServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.ImsJsonConverter;
import fi.livi.digitraffic.tie.service.trafficmessage.V2Datex2JsonConverter;
import fi.livi.digitraffic.tie.service.v1.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.service.v1.SensorDataUpdateService;
import fi.livi.digitraffic.tie.service.v1.TmsDataService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageReader;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageS3Writer;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateHandler;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.v1.location.LocationMetadataUpdater;
import fi.livi.digitraffic.tie.service.v1.location.LocationService;
import fi.livi.digitraffic.tie.service.v1.location.LocationSubtypeUpdater;
import fi.livi.digitraffic.tie.service.v1.location.LocationTypeUpdater;
import fi.livi.digitraffic.tie.service.v1.location.LocationUpdater;
import fi.livi.digitraffic.tie.service.v1.location.MetadataFileFetcher;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationService;
import fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryGitClient;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingDataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;
import fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper;
import fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService;

@Import({// configurations
          AmazonS3ClientTestConfiguration.class, S3PropertiesConfiguration.class, PropertiesConfiguration.class, JacksonAutoConfiguration.class,
          Datex2XmlStringToObjectMarshaller.class, XmlMarshallerConfiguration.class, RestTemplate.class, RetryTemplate.class,

          // services
          LocationService.class, CameraPresetService.class, TmsStationService.class, DataStatusService.class,
          RoadStationService.class, FreeFlowSpeedService.class, TmsStationSensorConstantService.class, RoadStationSensorService.class,
          TmsDataService.class, CameraImageUpdateHandler.class, CameraImageReader.class, CameraImageS3Writer.class, FileHttpGetClient.class,
          CameraPresetHistoryUpdateService.class, FlywayService.class,
          WeatherService.class, SensorDataUpdateService.class,
          ImsJsonConverter.class, V2Datex2UpdateService.class,
          V2Datex2JsonConverter.class,
          V3RegionGeometryUpdateService.class,
          V3MaintenanceTrackingUpdateService.class,
          LocationTypeUpdater.class, LocationMetadataUpdater.class, LocationUpdater.class, LocationSubtypeUpdater.class,
          MetadataFileFetcher.class, ClusteredLocker.class, LockingServiceInternal.class,
          V3RegionGeometryDataService.class, V2MaintenanceTrackingDataService.class,

          // V1 services
          RoadStationSensorServiceV1.class, TmsDataWebServiceV1.class, TmsStationMetadataWebServiceV1.class,

          // converters
          TmsStationMetadata2FeatureConverter.class, CoordinateConverter.class, StationSensorConverterService.class,
          ObjectMapper.class,

          // daos
          TmsSensorConstantDao.class, SensorValueDao.class, RoadStationDao.class, SensorValueHistoryDao.class,
          LockingDao.class,

          // Test services etc.
          TrafficMessageTestHelper.class, V3MaintenanceTrackingServiceTestHelper.class, TmsTestHelper.class
})
public abstract class AbstractServiceTest extends AbstractJpaTest {

    @MockBean
    protected RegionGeometryGitClient regionGeometryGitClientMock;

    @SpyBean
    protected MetadataFileFetcher metadataFileFetcherSpy;

}
