package fi.livi.digitraffic.tie;

import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.conf.amazon.AmazonS3ClientTestConfiguration;
import fi.livi.digitraffic.tie.conf.amazon.S3PropertiesConfiguration;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.conf.properties.PropertiesConfiguration;
import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.converter.feature.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.converter.tms.v1.TmsStationToFeatureConverterV1;
import fi.livi.digitraffic.tie.dao.v1.RoadStationDao;
import fi.livi.digitraffic.tie.dao.v1.SensorValueDao;
import fi.livi.digitraffic.tie.dao.v1.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.TmsTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsDataWebServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.ImsJsonConverter;
import fi.livi.digitraffic.tie.service.trafficmessage.TrafficMessageJsonConverterV1;
import fi.livi.digitraffic.tie.service.trafficmessage.V2Datex2JsonConverter;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2DataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

@DataJpaTest(properties = "spring.main.web-application-type=servlet",
             excludeAutoConfiguration = { FlywayAutoConfiguration.class, LiquibaseAutoConfiguration.class,
                                          TestDatabaseAutoConfiguration.class, DataSourceAutoConfiguration.class},
             showSql = false)
@Import({// configurations
         AmazonS3ClientTestConfiguration.class, S3PropertiesConfiguration.class, PropertiesConfiguration.class, JacksonAutoConfiguration.class,
         Datex2XmlStringToObjectMarshaller.class, XmlMarshallerConfiguration.class, RestTemplate.class, RetryTemplate.class,

         // Services V1
         TmsDataWebServiceV1.class, TmsStationMetadataWebServiceV1.class,
         RoadStationSensorServiceV1.class,

         // Services
         TmsStationService.class, DataStatusService.class, TmsStationSensorConstantService.class, StationSensorConverterService.class,
         V3RegionGeometryDataService.class, V3Datex2DataService.class, V2Datex2DataService.class,

         // Repositories and daos
         TmsSensorConstantDao.class, SensorValueDao.class, RoadStationDao.class,

         // Conveters
         TmsStationMetadata2FeatureConverter.class, TmsStationToFeatureConverterV1.class, CoordinateConverter.class,
         TrafficMessageJsonConverterV1.class, V2Datex2JsonConverter.class, ImsJsonConverter.class,

         // Test services
         TmsTestHelper.class, TrafficMessageTestHelper.class,
})
public abstract class AbstractWebServiceTest extends AbstractJpaTest {
}