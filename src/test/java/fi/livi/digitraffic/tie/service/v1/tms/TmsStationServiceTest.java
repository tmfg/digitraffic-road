package fi.livi.digitraffic.tie.service.v1.tms;

import static fi.livi.digitraffic.tie.controller.RoadStationState.ACTIVE;
import static fi.livi.digitraffic.tie.controller.RoadStationState.ALL;
import static fi.livi.digitraffic.tie.controller.RoadStationState.REMOVED;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.v1.TmsStation;

public class TmsStationServiceTest extends AbstractServiceTest {

    @Autowired
    private TmsStationService tmsStationService;

    private Long tmsNaturalId;
    private Long rsNaturalId;

    @BeforeEach
    public void initData() {
        // 4 active stations
        TestUtils.generateDummyTmsStations(4).forEach(s -> {
            s.getRoadStation().getRoadAddress().setRoadNumber(40);
            tmsNaturalId = s.getNaturalId();
            rsNaturalId = s.getRoadStation().getNaturalId();
            entityManager.persist(s);
        });
        // 3 temporarily removed stations
        TestUtils.generateDummyTmsStations(3).stream()
            .forEach(s -> {
                s.getRoadStation().getRoadAddress().setRoadNumber(30);
                s.getRoadStation().setCollectionStatus(CollectionStatus.REMOVED_TEMPORARILY);
                entityManager.persist(s);
            });
        // 2 permanently removed stations
        TestUtils.generateDummyTmsStations(2).forEach(s -> {
            s.getRoadStation().getRoadAddress().setRoadNumber(20);
            s.getRoadStation().setCollectionStatus(CollectionStatus.REMOVED_PERMANENTLY);
            entityManager.persist(s);
        });
        entityManager.flush();
    }

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollection() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, ACTIVE);
        // GATHERING + REMOVED_TEMPORARILY
        assertCollectionSize(7, stations.getFeatures());
    }

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollectionOnlyUpdateInfo() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(true, ACTIVE);

        assertCollectionSize(0, stations.getFeatures());
    }

    @Test
    public void findPermanentlyRemovedStations() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, REMOVED);

        assertCollectionSize(2, stations.getFeatures());
    }

    @Test
    public void findAllStations() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, ALL);

        assertCollectionSize(9, stations.getFeatures());
    }

    @Test
    public void findAllTmsStationsMappedByByTmsNaturalId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsMappedByByTmsNaturalId();

        assertCollectionSize(9, stations.entrySet());
    }

    @Test
    public void findAllTmsStationsByMappedByLotjuId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsByMappedByLotjuId();

        assertCollectionSize(9, stations.entrySet());
    }

    @Test
    public void listTmsStationsByRoadNumber() {
        final TmsStationFeatureCollection tmsStationFeatures = tmsStationService.listTmsStationsByRoadNumber(40, ACTIVE);

        assertCollectionSize(4, tmsStationFeatures.getFeatures());
    }

    @Test
    public void getTmsStationByRoadStationId() {
        final TmsStationFeature tmsStation = tmsStationService.getTmsStationByRoadStationId(rsNaturalId);

        assertNotNull(tmsStation);
    }

    @Test
    public void getTmsStationByLamId() {
        final TmsStationFeature tmsStation = tmsStationService.getTmsStationByLamId(tmsNaturalId);

        assertNotNull(tmsStation);
    }

}
