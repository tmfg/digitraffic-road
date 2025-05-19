package fi.livi.digitraffic.tie.service.weather.forecast;

import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN_DOUBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureV1;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.service.weather.forecast.v1.ForecastWebDataServiceV1;
import okhttp3.mockwebserver.MockWebServer;

public class ForecastSectionMetadataUpdaterTest extends AbstractDaemonTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastWebDataServiceV1 v2ForecastSectionMetadataService;

    @Autowired
    private ForecastSectionMetadataUpdateService forecastSectionMetadataUpdateService;

    private MockWebServer server;

    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;

    private ForecastSectionV2MetadataUpdater forecastSectionV2MetadataUpdater;

    final org.locationtech.jts.geom.Geometry AREA =
        PostgisGeometryUtils.createSquarePolygonFromMinMax(X_MIN_DOUBLE, X_MAX_DOUBLE,
                                                           Y_MIN_DOUBLE, Y_MAX_DOUBLE);

    @BeforeEach
    public void before() {
        server = new MockWebServer();

        final ForecastSectionClient forecastSectionClient = forecastSectionTestHelper.createForecastSectionClient(server);

        forecastSectionV2MetadataUpdater =
            new ForecastSectionV2MetadataUpdater(forecastSectionClient, forecastSectionMetadataUpdateService);
    }

    @AfterEach
    public void after() {
        forecastSectionRepository.deleteAllInBatch();
    }

    @Test
    public void updateForecastSectionV2MetadataSucceeds() throws IOException {
        forecastSectionTestHelper.serveGzippedMetadata(server, 2);

        forecastSectionV2MetadataUpdater.updateForecastSectionsV2Metadata();

        final ForecastSectionFeatureCollectionV1 featureCollection =
            v2ForecastSectionMetadataService.findForecastSections(false,false, null,
                                                                  X_MIN_DOUBLE, Y_MIN_DOUBLE,
                                                                  X_MAX_DOUBLE, Y_MAX_DOUBLE);
        final Instant lastModified = forecastSectionRepository.getLastModified(2, AREA, null);
        assertEquals(lastModified, featureCollection.dataUpdatedTime);

        final ForecastSectionFeatureV1 feature = featureCollection.getFeatures().getFirst();
        assertEquals(11, featureCollection.getFeatures().size());
        assertEquals("00004_229_00307_1_0", feature.getProperties().id);

        final List<List<List<Double>>> coordinates = (List<List<List<Double>>>) feature.getGeometry().getCoordinates();
        assertEquals(2, coordinates.getFirst().size());

        assertCoordinates(25.9564265, coordinates.getFirst().getFirst().getFirst());
        assertCoordinates(62.1203392, coordinates.getFirst().getFirst().get(1));
        assertCoordinates(25.9563029, coordinates.getFirst().get(1).getFirst());
        assertCoordinates(62.1203895, coordinates.getFirst().get(1).get(1));
        assertEquals(76, coordinates.get(15).size());
        assertCoordinates(25.9721482, coordinates.get(15).getFirst().getFirst());
        assertCoordinates(62.1119208, coordinates.get(15).getFirst().get(1));
        assertCoordinates(25.9564265, coordinates.get(15).get(75).getFirst());
        assertCoordinates(62.1203392, coordinates.get(15).get(75).get(1));

        assertEquals(2, feature.getProperties().roadSegments.size());
        assertEquals(307, feature.getProperties().roadSegments.getFirst().startDistance.intValue());
        assertEquals(2830, feature.getProperties().roadSegments.getFirst().endDistance.intValue());

        assertEquals(13, feature.getProperties().linkIds.size());
        assertEquals(5742592L, feature.getProperties().linkIds.getFirst().longValue());
        assertEquals(12471709L, feature.getProperties().linkIds.get(12).longValue());
    }

    @Test
    public void findForecastSectionsByRoadNumberSucceeds() throws IOException {
        forecastSectionTestHelper.serveGzippedMetadata(server, 2);

        forecastSectionV2MetadataUpdater.updateForecastSectionsV2Metadata();

        final ForecastSectionFeatureCollectionV1 featureCollection =
            v2ForecastSectionMetadataService.findForecastSections(false, false, 941,
                                                                  X_MIN_DOUBLE, Y_MIN_DOUBLE,
                                                                  X_MAX_DOUBLE, Y_MAX_DOUBLE);

        assertEquals(1, featureCollection.getFeatures().size());

        final ForecastSectionFeatureV1 feature1 = featureCollection.getFeatures().getFirst();

        assertEquals("00941_010_00000_0_0", feature1.getProperties().id);
        assertEquals("Posiontie, Ranuantie 941.10", feature1.getProperties().description);
        assertEquals(1, feature1.getProperties().roadSegments.size());
        assertEquals(9, feature1.getProperties().linkIds.size());
        assertEquals(Geometry.Type.MultiLineString, feature1.getGeometry().getType());
        assertEquals(11, feature1.getGeometry().getCoordinates().size());
        assertEquals(22, ((List<Double>)feature1.getGeometry().getCoordinates().getFirst()).size());
        assertCoordinates(27.3965783, ((List<List<Double>>)feature1.getGeometry().getCoordinates().getFirst()).getFirst().getFirst());
        assertCoordinates(65.9882322, ((List<List<Double>>)feature1.getGeometry().getCoordinates().getFirst()).getFirst().get(1));
        assertCoordinates(27.4148914, ((List<List<Double>>)feature1.getGeometry().getCoordinates().get(10)).get(18).getFirst());
        assertCoordinates(65.9934206, ((List<List<Double>>)feature1.getGeometry().getCoordinates().get(10)).get(18).get(1));
    }

    @Test
    public void findForecastSectionsByNaturalIdSucceeds() throws IOException {
        forecastSectionTestHelper.serveGzippedMetadata(server, 2);

        forecastSectionV2MetadataUpdater.updateForecastSectionsV2Metadata();

        final ForecastSectionFeatureV1 feature = v2ForecastSectionMetadataService.getForecastSectionById(false,"00941_010_00000_0_0");

        assertEquals("00941_010_00000_0_0", feature.getProperties().id);
    }

    private void assertCoordinates(final double expected, final double actual) {
        assertEquals(expected, actual, 0.000001); // 6 digits
    }

}
