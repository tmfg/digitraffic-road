package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.v2.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataUpdater;

public class V2ForecastSectionMetadataUpdaterTest extends AbstractDaemonTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private V2ForecastSectionMetadataService v2ForecastSectionMetadataService;

    @Autowired
    private V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;

    @Autowired
    private DataStatusService dataStatusService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;

    @Autowired
    private ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private V2ForecastSectionMetadataUpdater forecastSectionMetadataUpdaterMockRealMethods;


    @BeforeEach
    public void before() {
        forecastSectionMetadataUpdaterMockRealMethods =
            new V2ForecastSectionMetadataUpdater(forecastSectionClient, forecastSectionRepository,
                                                 v2ForecastSectionMetadataDao,dataStatusService);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void after() {
        forecastSectionRepository.deleteAllInBatch();
    }

    @Test
    public void updateForecastSectionV2MetadataSucceeds() {
        forecastSectionTestHelper.serverExpectMetadata(server, 2);

        final Instant updated = forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionsV2Metadata();
        final Instant lastUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_V2_METADATA).toInstant();
        final Instant lastChecked = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_V2_METADATA_CHECK).toInstant();

        assertEquals(updated, lastUpdated);

        final ForecastSectionV2FeatureCollection featureCollection =
            v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, null, null, null, null, null,
                                                                          null);

        assertEquals(updated, featureCollection.dataUpdatedTime.toInstant());
        assertEquals(lastChecked.getEpochSecond(), featureCollection.dataLastCheckedTime.toEpochSecond(), 2);

        final ForecastSectionV2Feature feature = featureCollection.getFeatures().get(0);
        assertEquals(11, featureCollection.getFeatures().size());
        assertEquals("00004_229_00307_1_0", feature.getProperties().getNaturalId());

        final List<List<List<Double>>> coordinates = feature.getGeometry().getCoordinates();
        assertEquals(2, coordinates.get(0).size());

        assertCoordinates(25.9564265, coordinates.get(0).get(0).get(0));
        assertCoordinates(62.1203392, coordinates.get(0).get(0).get(1));
        assertCoordinates(25.9563029, coordinates.get(0).get(1).get(0));
        assertCoordinates(62.1203895, coordinates.get(0).get(1).get(1));
        assertEquals(76, coordinates.get(15).size());
        assertCoordinates(25.9721482, coordinates.get(15).get(0).get(0));
        assertCoordinates(62.1119208, coordinates.get(15).get(0).get(1));
        assertCoordinates(25.9564265, coordinates.get(15).get(75).get(0));
        assertCoordinates(62.1203392, coordinates.get(15).get(75).get(1));

        assertEquals(2, feature.getProperties().getRoadSegments().size());
        assertEquals(307, feature.getProperties().getRoadSegments().get(0).getStartDistance().intValue());
        assertEquals(2830, feature.getProperties().getRoadSegments().get(0).getEndDistance().intValue());

        assertEquals(13, feature.getProperties().getLinkIdList().size());
        assertEquals(5742592L, feature.getProperties().getLinkIdList().get(0).longValue());
        assertEquals(12471709L, feature.getProperties().getLinkIdList().get(12).longValue());
    }

    @Test
    public void findForecastSectionsByRoadNumberSucceeds() {

        forecastSectionTestHelper.serverExpectMetadata(server, 2);

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionsV2Metadata();

        final ForecastSectionV2FeatureCollection featureCollection = v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, 941,
                                                                                                                                   null, null,
                                                                                                                                   null, null,
                                                                                                                                   null);

        assertEquals(1, featureCollection.getFeatures().size());

        final ForecastSectionV2Feature feature1 = featureCollection.getFeatures().get(0);

        assertEquals("00941_010_00000_0_0", feature1.getProperties().getNaturalId());
        assertEquals("Posiontie, Ranuantie 941.10", feature1.getProperties().getDescription());
        assertEquals(1, feature1.getProperties().getRoadSegments().size());
        assertEquals(9, feature1.getProperties().getLinkIdList().size());
        assertEquals(Geometry.Type.MultiLineString, feature1.getGeometry().getType());
        assertEquals(11, feature1.getGeometry().getCoordinates().size());
        assertEquals(22, feature1.getGeometry().getCoordinates().get(0).size());
        assertCoordinates(27.3965783, feature1.getGeometry().getCoordinates().get(0).get(0).get(0));
        assertCoordinates(65.9882322, feature1.getGeometry().getCoordinates().get(0).get(0).get(1));
        assertCoordinates(27.4148914, feature1.getGeometry().getCoordinates().get(10).get(18).get(0));
        assertCoordinates(65.9934206, feature1.getGeometry().getCoordinates().get(10).get(18).get(1));
    }

    @Test
    public void findForecastSectionsByNaturalIdSucceeds() {

        forecastSectionTestHelper.serverExpectMetadata(server, 2);

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionsV2Metadata();

        final ForecastSectionV2FeatureCollection featureCollection = v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, null,
                                                                                                                                   null, null,
                                                                                                                                   null, null,
                                                                                                                                   List.of("00941_010_00000_0_0"));

        assertEquals(1, featureCollection.getFeatures().size());
        assertEquals("00941_010_00000_0_0", featureCollection.getFeatures().get(0).getProperties().getNaturalId());
    }

    private void assertCoordinates(final double expected, final double actual) {
        assertEquals(expected, actual, 0.000001); // 6 digits
    }

}