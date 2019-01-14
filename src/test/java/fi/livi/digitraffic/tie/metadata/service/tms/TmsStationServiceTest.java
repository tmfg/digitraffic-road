package fi.livi.digitraffic.tie.metadata.service.tms;

import static fi.livi.digitraffic.tie.metadata.controller.TmsState.ACTIVE;
import static fi.livi.digitraffic.tie.metadata.controller.TmsState.ALL;
import static fi.livi.digitraffic.tie.metadata.controller.TmsState.REMOVED;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.converter.NonPublicRoadStationException;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

public class TmsStationServiceTest extends AbstractTest {

    @Autowired
    private TmsStationService tmsStationService;

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollection() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, ACTIVE);

        assertCollectionSize(488, stations.getFeatures());
    }

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollectionOnlyUpdateInfo() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(true, ACTIVE);

        assertCollectionSize(0, stations.getFeatures());
    }

    @Test
    public void findPermanentlyRemovedStations() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, REMOVED);

        assertCollectionSize(49, stations.getFeatures());
    }

    @Test
    public void findAllStations() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, ALL);

        assertCollectionSize(537, stations.getFeatures());
    }

    @Test
    public void findAllTmsStationsMappedByByTmsNaturalId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsMappedByByTmsNaturalId();

        assertCollectionSize(545, stations.entrySet());
    }

    @Test
    public void findAllTmsStationsByMappedByLotjuId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsByMappedByLotjuId();

        assertCollectionSize(545, stations.entrySet());
    }

    @Test
    public void listTmsStationsByRoadNumber() {
        final TmsStationFeatureCollection tmsStationFeatures = tmsStationService.listTmsStationsByRoadNumber(10, ACTIVE);

        assertCollectionSize(5, tmsStationFeatures.getFeatures());
    }

    @Test
    public void getTmsStationByRoadStationId() throws NonPublicRoadStationException {
        final TmsStationFeature tmsStation = tmsStationService.getTmsStationByRoadStationId(23801L);

        Assert.assertNotNull(tmsStation);
    }

    @Test
    public void getTmsStationByLamId() throws NonPublicRoadStationException {
        final TmsStationFeature tmsStation = tmsStationService.getTmsStationByLamId(6L);

        Assert.assertNotNull(tmsStation);
    }

}
