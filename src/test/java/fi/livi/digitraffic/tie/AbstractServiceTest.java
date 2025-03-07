package fi.livi.digitraffic.tie;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.common.dao.LockingDao;
import fi.livi.digitraffic.tie.conf.amazon.AmazonS3ClientTestConfiguration;
import fi.livi.digitraffic.tie.conf.amazon.S3PropertiesConfiguration;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.conf.properties.PropertiesConfiguration;
import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationDao;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueDao;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryDao;
import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.FlywayService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.TmsTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.maintenance.MaintenanceTrackingMqttDataService;
import fi.livi.digitraffic.tie.service.maintenance.MaintenanceTrackingUpdateServiceV1;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1;
import fi.livi.digitraffic.tie.service.roadstation.SensorDataUpdateService;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.service.tms.TmsStationService;
import fi.livi.digitraffic.tie.service.tms.v1.TmsDataWebServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2UpdateService;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.ImsJsonConverter;
import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryGitClient;
import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryUpdateService;
import fi.livi.digitraffic.tie.service.trafficmessage.location.LocationMetadataUpdater;
import fi.livi.digitraffic.tie.service.trafficmessage.location.LocationSubtypeUpdater;
import fi.livi.digitraffic.tie.service.trafficmessage.location.LocationTypeUpdater;
import fi.livi.digitraffic.tie.service.trafficmessage.location.LocationUpdater;
import fi.livi.digitraffic.tie.service.trafficmessage.location.MetadataFileFetcher;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.location.LocationWebServiceV1;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageReader;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageS3Writer;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateHandler;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryUpdateService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;

@Import({// Configurations
         // Spring
         MetricsAutoConfiguration.class, // MeterRegistry
         SimpleMetricsExportAutoConfiguration.class, // MeterRegistry
         JacksonAutoConfiguration.class, // ObjectMapper

         //RetryTemplate.class,
         // Own configs
         AmazonS3ClientTestConfiguration.class, S3PropertiesConfiguration.class, PropertiesConfiguration.class,
         Datex2XmlStringToObjectMarshaller.class, XmlMarshallerConfiguration.class,

         // Services
         CameraPresetService.class, TmsStationService.class, DataStatusService.class,
         RoadStationService.class, TmsStationSensorConstantService.class, RoadStationSensorService.class,
         CameraImageUpdateHandler.class, CameraImageReader.class, CameraImageS3Writer.class,
         CameraPresetHistoryUpdateService.class, FlywayService.class,
         SensorDataUpdateService.class,
         ImsJsonConverter.class, Datex2UpdateService.class,
         RegionGeometryUpdateService.class,
         MaintenanceTrackingUpdateServiceV1.class,
         LocationTypeUpdater.class, LocationMetadataUpdater.class, LocationUpdater.class, LocationSubtypeUpdater.class,
         MetadataFileFetcher.class,

         // V1 services
         RoadStationSensorServiceV1.class, TmsDataWebServiceV1.class, TmsStationMetadataWebServiceV1.class,
         MaintenanceTrackingMqttDataService.class, LocationWebServiceV1.class,

         // Converters
         CoordinateConverter.class, StationSensorConverterService.class,
         ObjectMapper.class,

         // Daos
         TmsSensorConstantDao.class, SensorValueDao.class, RoadStationDao.class, SensorValueHistoryDao.class,
         LockingDao.class,

         // Test services etc.
         TrafficMessageTestHelper.class, MaintenanceTrackingServiceTestHelperV1.class, TmsTestHelper.class
})
public abstract class AbstractServiceTest extends AbstractJpaTest {

    @MockitoBean
    protected RegionGeometryGitClient regionGeometryGitClientMock;

    @MockitoSpyBean
    protected MetadataFileFetcher metadataFileFetcherSpy;

}
