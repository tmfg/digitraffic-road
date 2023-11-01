package fi.livi.digitraffic.tie.service.tms.v1;

import static fi.livi.digitraffic.tie.controller.RoadStationState.ACTIVE;
import static fi.livi.digitraffic.tie.controller.RoadStationState.ALL;
import static fi.livi.digitraffic.tie.controller.RoadStationState.REMOVED;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractWebServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;

/** Test for {@link TmsStationMetadataWebServiceV1} */
public class TmsStationMetadataWebServiceV1Test extends AbstractWebServiceTest {

    @Autowired
    private TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1;

    private Long rsNaturalId;

    @BeforeEach
    public void initData() {
        // 4 active stations
        TestUtils.generateDummyTmsStations(4).forEach(s -> {
            s.getRoadStation().getRoadAddress().setRoadNumber(40);
            rsNaturalId = s.getRoadStation().getNaturalId();
            entityManager.persist(s);
        });
        // 3 temporarily removed stations
        TestUtils.generateDummyTmsStations(3)
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
        final TmsStationFeatureCollectionSimpleV1 stations = tmsStationMetadataWebServiceV1.findAllPublishableTmsStationsAsSimpleFeatureCollection(false, ACTIVE);
        // GATHERING + REMOVED_TEMPORARILY
        assertCollectionSize(7, stations.getFeatures());
    }

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollectionOnlyUpdateInfo() {
        final TmsStationFeatureCollectionSimpleV1 stations = tmsStationMetadataWebServiceV1.findAllPublishableTmsStationsAsSimpleFeatureCollection(true, ACTIVE);

        assertCollectionSize(0, stations.getFeatures());
    }

    @Test
    public void findPermanentlyRemovedStations() {
        final TmsStationFeatureCollectionSimpleV1 stations = tmsStationMetadataWebServiceV1.findAllPublishableTmsStationsAsSimpleFeatureCollection(false, REMOVED);

        assertCollectionSize(2, stations.getFeatures());
    }

    @Test
    public void findAllStations() {
        final TmsStationFeatureCollectionSimpleV1 stations = tmsStationMetadataWebServiceV1.findAllPublishableTmsStationsAsSimpleFeatureCollection(false, ALL);

        assertCollectionSize(9, stations.getFeatures());
    }

    @Test
    public void getTmsStationByRoadStationId() {
        final TmsStationFeatureDetailedV1 tmsStation = tmsStationMetadataWebServiceV1.getTmsStationById(rsNaturalId);

        assertNotNull(tmsStation);
    }
}
