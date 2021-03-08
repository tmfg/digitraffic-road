package fi.livi.digitraffic.tie;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.conf.amazon.AmazonS3ClientTestConfiguration;
import fi.livi.digitraffic.tie.conf.amazon.S3PropertiesConfig;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.converter.StationSensorConverter;
import fi.livi.digitraffic.tie.converter.feature.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.dao.LockingDao;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryDao;
import fi.livi.digitraffic.tie.dao.v1.RoadStationDao;
import fi.livi.digitraffic.tie.dao.v1.SensorValueDao;
import fi.livi.digitraffic.tie.dao.v1.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.helper.FileGetService;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.FlywayService;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.LockingServiceInternal;
import fi.livi.digitraffic.tie.service.RoadDistrictService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.datex2.ImsJsonConverterService;
import fi.livi.digitraffic.tie.service.datex2.V2Datex2JsonConverterService;
import fi.livi.digitraffic.tie.service.v1.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.service.v1.SensorDataUpdateService;
import fi.livi.digitraffic.tie.service.v1.TmsDataService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageReader;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageS3Writer;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService;
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
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;

@Import({ Datex2XmlStringToObjectMarshaller.class, XmlMarshallerConfiguration.class, RestTemplate.class, RetryTemplate.class,
          // services
          LocationService.class, RoadDistrictService.class, CameraPresetService.class, TmsStationService.class, DataStatusService.class,
          RoadStationService.class, FreeFlowSpeedService.class, TmsStationSensorConstantService.class, RoadStationSensorService.class,
          TmsDataService.class, CameraImageUpdateService.class, CameraImageReader.class, CameraImageS3Writer.class, FileGetService.class,
          CameraPresetHistoryUpdateService.class, FlywayService.class,
          WeatherService.class, SensorDataUpdateService.class,
          JacksonAutoConfiguration.class,
          ImsJsonConverterService.class, V2Datex2UpdateService.class,
          V2Datex2JsonConverterService.class,
          V3RegionGeometryUpdateService.class,
          V2MaintenanceTrackingServiceTestHelper.class, V2MaintenanceTrackingUpdateService.class,
          LocationTypeUpdater.class, LocationMetadataUpdater.class, LocationUpdater.class, LocationSubtypeUpdater.class,
          MetadataFileFetcher.class, LockingService.class, LockingServiceInternal.class,

          // converters
          TmsStationMetadata2FeatureConverter.class, CoordinateConverter.class, StationSensorConverter.class,
          ObjectMapper.class,

          // daos
          TmsSensorConstantDao.class, SensorValueDao.class, RoadStationDao.class, SensorValueHistoryDao.class,
          LockingDao.class,

          // configurations
          AmazonS3ClientTestConfiguration.class, S3PropertiesConfig.class
        })
@TestPropertySource(properties = { "spring.localstack.enabled=false" })
public abstract class AbstractServiceTest extends AbstractJpaTest {
}
