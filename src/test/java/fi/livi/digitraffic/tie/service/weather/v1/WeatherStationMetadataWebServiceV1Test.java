package fi.livi.digitraffic.tie.service.weather.v1;

import static fi.livi.digitraffic.test.util.AssertUtil.assertCollectionSize;
import static fi.livi.digitraffic.tie.controller.RoadStationState.ACTIVE;
import static fi.livi.digitraffic.tie.controller.RoadStationState.ALL;
import static fi.livi.digitraffic.tie.controller.RoadStationState.REMOVED;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractWebServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;

/** Test for {@link WeatherStationMetadataWebServiceV1} */
public class WeatherStationMetadataWebServiceV1Test extends AbstractWebServiceTest {

    @Autowired
    private WeatherStationMetadataWebServiceV1 weatherStationMetadataWebServiceV1;

    private Long rsNaturalId;

    @BeforeEach
    public void initData() {
        // 4 active stations
        TestUtils.generateDummyWeatherStations(4).forEach(s -> {
            s.getRoadStation().getRoadAddress().setRoadNumber(40);
            rsNaturalId = s.getRoadStation().getNaturalId();
            entityManager.persist(s);
        });
        // 3 temporarily removed stations
        TestUtils.generateDummyWeatherStations(3)
            .forEach(s -> {
                s.getRoadStation().getRoadAddress().setRoadNumber(30);
                s.getRoadStation().setCollectionStatus(CollectionStatus.REMOVED_TEMPORARILY);
                entityManager.persist(s);
            });
        // 2 permanently removed stations
        TestUtils.generateDummyWeatherStations(2).forEach(s -> {
            s.getRoadStation().getRoadAddress().setRoadNumber(20);
            s.getRoadStation().setCollectionStatus(CollectionStatus.REMOVED_PERMANENTLY);
            entityManager.persist(s);
        });
        entityManager.flush();
    }

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollection() {
        final WeatherStationFeatureCollectionSimpleV1 stations = weatherStationMetadataWebServiceV1.findAllPublishableWeatherStationsAsSimpleFeatureCollection(false, ACTIVE);
        // GATHERING + REMOVED_TEMPORARILY
        assertCollectionSize(7, stations.getFeatures());
    }

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollectionOnlyUpdateInfo() {
        final WeatherStationFeatureCollectionSimpleV1 stations = weatherStationMetadataWebServiceV1.findAllPublishableWeatherStationsAsSimpleFeatureCollection(true, ACTIVE);

        assertCollectionSize(0, stations.getFeatures());
    }

    @Test
    public void findPermanentlyRemovedStations() {
        final WeatherStationFeatureCollectionSimpleV1 stations = weatherStationMetadataWebServiceV1.findAllPublishableWeatherStationsAsSimpleFeatureCollection(false, REMOVED);

        assertCollectionSize(2, stations.getFeatures());
    }

    @Test
    public void findAllStations() {
        final WeatherStationFeatureCollectionSimpleV1 stations = weatherStationMetadataWebServiceV1.findAllPublishableWeatherStationsAsSimpleFeatureCollection(false, ALL);

        assertCollectionSize(9, stations.getFeatures());
    }

    @Test
    public void getTmsStationByRoadStationId() {
        final WeatherStationFeatureDetailedV1 tmsStation = weatherStationMetadataWebServiceV1.getWeatherStationById(rsNaturalId);

        assertNotNull(tmsStation);
    }
}
