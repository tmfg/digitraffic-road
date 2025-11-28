package fi.livi.digitraffic.tie;

import fi.livi.digitraffic.tie.conf.amazon.AmazonS3ClientTestConfiguration;
import fi.livi.digitraffic.tie.service.aws.S3Service;

import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;

import fi.livi.digitraffic.tie.conf.RoadCacheConfiguration;
import fi.livi.digitraffic.tie.conf.amazon.S3PropertiesConfiguration;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.conf.properties.PropertiesConfiguration;
import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.converter.tms.v1.TmsStationToFeatureConverterV1;
import fi.livi.digitraffic.tie.converter.weather.v1.WeatherStationToFeatureConverterV1;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingDao;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationDao;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueDao;
import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.TmsTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.maintenance.MaintenanceTrackingMqttDataService;
import fi.livi.digitraffic.tie.service.maintenance.MaintenanceTrackingUpdateServiceV1;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingWebDataServiceV1;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.service.tms.TmsStationService;
import fi.livi.digitraffic.tie.service.tms.v1.TmsDataWebServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII223XmlMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.ImsJsonConverter;
import fi.livi.digitraffic.tie.service.trafficmessage.TrafficMessageImsJsonConverterV1;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.RegionGeometryDataServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.TrafficMessageDataServiceV1;
import fi.livi.digitraffic.tie.service.weather.WeatherStationService;
import fi.livi.digitraffic.tie.service.weather.v1.WeatherDataWebServiceV1;
import fi.livi.digitraffic.tie.service.weather.v1.WeatherStationMetadataWebServiceV1;

@DataJpaTest(properties = "spring.main.web-application-type=servlet",
             excludeAutoConfiguration = { FlywayAutoConfiguration.class, LiquibaseAutoConfiguration.class,
                                          TestDatabaseAutoConfiguration.class, DataSourceAutoConfiguration.class},
             showSql = false)
@Import({// configurations
         AmazonS3ClientTestConfiguration.class, S3PropertiesConfiguration.class, PropertiesConfiguration.class, JacksonAutoConfiguration.class,
         DatexII223XmlMarshaller.class, XmlMarshallerConfiguration.class, RetryTemplate.class,
         RoadCacheConfiguration.class,

         // Services V1
         TmsDataWebServiceV1.class, TmsStationMetadataWebServiceV1.class, WeatherDataWebServiceV1.class, WeatherStationMetadataWebServiceV1.class,
         RoadStationSensorServiceV1.class, MaintenanceTrackingWebDataServiceV1.class, MaintenanceTrackingMqttDataService.class,
         RegionGeometryDataServiceV1.class, TrafficMessageDataServiceV1.class,

         // Old Services
         TmsStationService.class, DataStatusService.class, TmsStationSensorConstantService.class, StationSensorConverterService.class,
         WeatherStationService.class, S3Service.class,

         // Repositories and daos
         TmsSensorConstantDao.class, SensorValueDao.class, RoadStationDao.class, MaintenanceTrackingDao.class,

         // Converters
         TmsStationToFeatureConverterV1.class,
         WeatherStationToFeatureConverterV1.class,
         TrafficMessageImsJsonConverterV1.class, ImsJsonConverter.class, CoordinateConverter.class,

         // Test helpers etc.
         TmsTestHelper.class, TrafficMessageTestHelper.class, MaintenanceTrackingServiceTestHelperV1.class, MaintenanceTrackingUpdateServiceV1.class
})
public abstract class AbstractWebServiceTest extends AbstractJpaTest {
}
