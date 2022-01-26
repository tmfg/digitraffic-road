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

        assertEquals(updated, lastUpdated);

        final ForecastSectionV2FeatureCollection featureCollection =
            v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, null, null, null, null, null,
                                                                          null);
        final Instant now = Instant.now();
        assertEquals(updated, featureCollection.dataUpdatedTime.toInstant());
        assertEquals(now.getEpochSecond(), featureCollection.dataLastCheckedTime.toEpochSecond(), 2);

        final ForecastSectionV2Feature feature = featureCollection.getFeatures().get(0);
        assertEquals(3, featureCollection.getFeatures().size());
        assertEquals("00003_218_04302_0_0", feature.getProperties().getNaturalId());

        final List<List<List<Double>>> coordinates = feature.getGeometry().getCoordinates();
        assertEquals(9, coordinates.get(0).size());

        assertCoordinates(22.9983705, coordinates.get(0).get(0).get(0));
        assertCoordinates(62.1215860, coordinates.get(0).get(0).get(1));
        assertCoordinates(22.9980918, coordinates.get(0).get(1).get(0));
        assertCoordinates(62.1217175, coordinates.get(0).get(1).get(1));
        assertEquals(2, coordinates.get(15).size());
        assertCoordinates(22.9800960, coordinates.get(15).get(0).get(0));
        assertCoordinates(62.1412124, coordinates.get(15).get(0).get(1));
        assertCoordinates(22.9800859, coordinates.get(15).get(1).get(0));
        assertCoordinates(62.1414056, coordinates.get(15).get(1).get(1));

        assertEquals(1, feature.getProperties().getRoadSegments().size());
        assertEquals(4302, feature.getProperties().getRoadSegments().get(0).getStartDistance().intValue());
        assertEquals(6829, feature.getProperties().getRoadSegments().get(0).getEndDistance().intValue());

        assertEquals(16, feature.getProperties().getLinkIdList().size());
        assertEquals(3878918L, feature.getProperties().getLinkIdList().get(0).longValue());
        assertEquals(3879183L, feature.getProperties().getLinkIdList().get(15).longValue());
    }

    @Test
    public void findForecastSectionsByRoadNumberSucceeds() {

        forecastSectionTestHelper.serverExpectMetadata(server, 2);

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionsV2Metadata();

        final ForecastSectionV2FeatureCollection featureCollection = v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, 3,
                                                                                                                                   null, null,
                                                                                                                                   null, null,
                                                                                                                                   null);

        assertEquals(2, featureCollection.getFeatures().size());

        final ForecastSectionV2Feature feature1 = featureCollection.getFeatures().get(0);
        final ForecastSectionV2Feature feature2 = featureCollection.getFeatures().get(1);

        assertEquals("00003_218_04302_0_0", feature1.getProperties().getNaturalId());
        assertEquals("Vaasantie 3.218", feature1.getProperties().getDescription());
        assertEquals(1, feature1.getProperties().getRoadSegments().size());
        assertEquals(16, feature1.getProperties().getLinkIdList().size());
        assertEquals(Geometry.Type.MultiLineString, feature1.getGeometry().getType());
        assertEquals(16, feature1.getGeometry().getCoordinates().size());
        assertEquals(9, feature1.getGeometry().getCoordinates().get(0).size());
        assertCoordinates(22.9983705, feature1.getGeometry().getCoordinates().get(0).get(0).get(0));
        assertCoordinates(62.1215860, feature1.getGeometry().getCoordinates().get(0).get(0).get(1));
        assertCoordinates(22.9800960, feature1.getGeometry().getCoordinates().get(14).get(2).get(0));
        assertCoordinates(62.1412124, feature1.getGeometry().getCoordinates().get(14).get(2).get(1));

        assertEquals("00003_226_00000_0_0", feature2.getProperties().getNaturalId());
        assertEquals("Tampereentie 3.226", feature2.getProperties().getDescription());
    }

    @Test
    public void findForecastSectionsByNaturalIdSucceeds() {

        forecastSectionTestHelper.serverExpectMetadata(server, 2);

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionsV2Metadata();

        final ForecastSectionV2FeatureCollection featureCollection = v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, null,
                                                                                                                                   null, null,
                                                                                                                                   null, null,
                                                                                                                                   List.of("00009_216_03050_0_0"));

        assertEquals(1, featureCollection.getFeatures().size());
        assertEquals("00009_216_03050_0_0", featureCollection.getFeatures().get(0).getProperties().getNaturalId());
    }

    private void assertCoordinates(final double expected, final double actual) {
        assertEquals(expected, actual, 0.00000001);
    }

}