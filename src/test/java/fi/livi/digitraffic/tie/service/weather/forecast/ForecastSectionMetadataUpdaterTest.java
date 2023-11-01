package fi.livi.digitraffic.tie.service.weather.forecast;

import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN_DOUBLE;
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
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureV1;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weather.forecast.v1.ForecastWebDataServiceV1;

public class ForecastSectionMetadataUpdaterTest extends AbstractDaemonTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastWebDataServiceV1 v2ForecastSectionMetadataService;

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
    private ForecastSectionV2MetadataUpdater forecastSectionMetadataUpdaterMockRealMethods;

    final org.locationtech.jts.geom.Geometry AREA =
        PostgisGeometryUtils.createSquarePolygonFromMinMax(X_MIN_DOUBLE, X_MAX_DOUBLE,
                                                           Y_MIN_DOUBLE, Y_MAX_DOUBLE);

    @BeforeEach
    public void before() {
        forecastSectionMetadataUpdaterMockRealMethods =
            new ForecastSectionV2MetadataUpdater(forecastSectionClient, forecastSectionRepository,
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

        final ForecastSectionFeatureCollectionV1 featureCollection =
            v2ForecastSectionMetadataService.findForecastSections(false,false, null,
                                                                  X_MIN_DOUBLE, Y_MIN_DOUBLE,
                                                                  X_MAX_DOUBLE, Y_MAX_DOUBLE);
        final Instant lastModified = forecastSectionRepository.getLastModified(2, AREA, null);
        assertEquals(lastModified, featureCollection.dataUpdatedTime);

        final ForecastSectionFeatureV1 feature = featureCollection.getFeatures().get(0);
        assertEquals(11, featureCollection.getFeatures().size());
        assertEquals("00004_229_00307_1_0", feature.getProperties().id);

        final List<List<List<Double>>> coordinates = (List<List<List<Double>>>) feature.getGeometry().getCoordinates();
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

        assertEquals(2, feature.getProperties().roadSegments.size());
        assertEquals(307, feature.getProperties().roadSegments.get(0).startDistance.intValue());
        assertEquals(2830, feature.getProperties().roadSegments.get(0).endDistance.intValue());

        assertEquals(13, feature.getProperties().linkIds.size());
        assertEquals(5742592L, feature.getProperties().linkIds.get(0).longValue());
        assertEquals(12471709L, feature.getProperties().linkIds.get(12).longValue());
    }

    @Test
    public void findForecastSectionsByRoadNumberSucceeds() {

        forecastSectionTestHelper.serverExpectMetadata(server, 2);

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionsV2Metadata();

        final ForecastSectionFeatureCollectionV1 featureCollection =
            v2ForecastSectionMetadataService.findForecastSections(false, false, 941,
                                                                  X_MIN_DOUBLE, Y_MIN_DOUBLE,
                                                                  X_MAX_DOUBLE, Y_MAX_DOUBLE);

        assertEquals(1, featureCollection.getFeatures().size());

        final ForecastSectionFeatureV1 feature1 = featureCollection.getFeatures().get(0);

        assertEquals("00941_010_00000_0_0", feature1.getProperties().id);
        assertEquals("Posiontie, Ranuantie 941.10", feature1.getProperties().description);
        assertEquals(1, feature1.getProperties().roadSegments.size());
        assertEquals(9, feature1.getProperties().linkIds.size());
        assertEquals(Geometry.Type.MultiLineString, feature1.getGeometry().getType());
        assertEquals(11, feature1.getGeometry().getCoordinates().size());
        assertEquals(22, ((List<Double>)feature1.getGeometry().getCoordinates().get(0)).size());
        assertCoordinates(27.3965783, ((List<List<Double>>)feature1.getGeometry().getCoordinates().get(0)).get(0).get(0));
        assertCoordinates(65.9882322, ((List<List<Double>>)feature1.getGeometry().getCoordinates().get(0)).get(0).get(1));
        assertCoordinates(27.4148914, ((List<List<Double>>)feature1.getGeometry().getCoordinates().get(10)).get(18).get(0));
        assertCoordinates(65.9934206, ((List<List<Double>>)feature1.getGeometry().getCoordinates().get(10)).get(18).get(1));
    }

    @Test
    public void findForecastSectionsByNaturalIdSucceeds() {

        forecastSectionTestHelper.serverExpectMetadata(server, 2);

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionsV2Metadata();

        final ForecastSectionFeatureV1 feature = v2ForecastSectionMetadataService.getForecastSectionById(false,"00941_010_00000_0_0");

        assertEquals("00941_010_00000_0_0", feature.getProperties().id);
    }

    private void assertCoordinates(final double expected, final double actual) {
        assertEquals(expected, actual, 0.000001); // 6 digits
    }

}