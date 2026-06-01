package fi.livi.digitraffic.tie.service.weather.forecast;

import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN_DOUBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.service.weather.forecast.v1.ForecastWebDataServiceV1;
import okhttp3.mockwebserver.MockWebServer;

public class ForecastSectionSimpleMetadataUpdaterTest extends AbstractDaemonTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastWebDataServiceV1 forecastWebDataServiceV1;

    @Autowired
    private ForecastSectionMetadataUpdateService forecastSectionMetadataUpdateService;

    private MockWebServer server;

    protected ForecastSectionV1MetadataUpdater forecastSectionV1MetadataUpdater;

    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;

    @BeforeEach
    public void before() {
        server = new MockWebServer();

        final ForecastSectionClient forecastSectionClient = forecastSectionTestHelper.createForecastSectionClient(server);

        forecastSectionV1MetadataUpdater =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionMetadataUpdateService);
    }

    @AfterEach
    public void after() {
        forecastSectionRepository.deleteAllInBatch();
    }

    @Test
    public void updateForecastSectionV1MetadataSucceeds() throws IOException {
        forecastSectionTestHelper.serveGzippedMetadata(server, 1);

        final Instant before = getTransactionTimestamp();
        forecastSectionV1MetadataUpdater.updateForecastSectionV1Metadata();
        final Instant after = getTransactionTimestamp();

        final ForecastSectionFeatureCollectionSimpleV1 collection =
            forecastWebDataServiceV1.findSimpleForecastSections(false, null,
                X_MIN_DOUBLE, Y_MIN_DOUBLE, X_MAX_DOUBLE, Y_MAX_DOUBLE);

        // dataUpdatedTime must be between before and after the update (allow 1s slack for transaction boundaries)
        assertFalse(collection.dataUpdatedTime.isBefore(before.minusSeconds(1)),
            "dataUpdatedTime should not be before update started");
        assertFalse(collection.dataUpdatedTime.isAfter(after.plusSeconds(1)),
            "dataUpdatedTime should not be after update finished");

        assertEquals(10, collection.getFeatures().size());
        assertEquals("00001_001_000_0", collection.getFeatures().getFirst().getProperties().id);
        assertEquals(10, collection.getFeatures().getFirst().getGeometry().getCoordinates().size());
        assertEquals(Double.parseDouble("24.944"), collection.getFeatures().getFirst().getGeometry().getCoordinates().getFirst().getFirst(), 0.01);
        assertEquals(Double.parseDouble("60.167"), collection.getFeatures().getFirst().getGeometry().getCoordinates().getFirst().get(1), 0.01);
    }
}
