package fi.livi.digitraffic.tie.scheduler;

import static fi.livi.digitraffic.tie.controller.RoadStationState.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.client.support.destination.DestinationProvider;

import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureSimpleV1;
import fi.livi.digitraffic.tie.dto.v1.TmsRoadStationSensorDto;
import fi.livi.digitraffic.tie.dto.v1.TmsRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.VehicleClass;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuLAMMetatiedotServiceEndpointMock;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataClient;
import fi.livi.digitraffic.tie.service.v1.tms.TmsSensorUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationsSensorsUpdater;

public class TmsStationMetadataUpdateJobTest extends AbstractMetadataUpdateJobTest {

    @Autowired
    private TmsSensorUpdater tmsSensorUpdater;

    @Autowired
    private TmsStationsSensorsUpdater tmsStationsSensorsUpdater;

    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceMock;

    @Autowired
    private LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    private TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1;
    private DestinationProvider originalDestinationProvider;

    @BeforeEach
    public void setFirstDestinationProviderForLotjuClients() {
        if (!isBeanRegistered(TmsStationMetadataWebServiceV1.class)) {
            final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1 = beanFactory.createBean(TmsStationMetadataWebServiceV1.class);
            beanFactory.registerSingleton(tmsStationMetadataWebServiceV1.getClass().getCanonicalName(), tmsStationMetadataWebServiceV1);
            this.tmsStationMetadataWebServiceV1 = tmsStationMetadataWebServiceV1;
        }
        setLotjuClientFirstDestinationProviderAndSaveOriginalToMap(lotjuTmsStationMetadataClient);
    }

    @AfterEach
    public void restoreOriginalDestinationProviderForLotjuClients() {
        restoreLotjuClientDestinationProvider(lotjuTmsStationMetadataClient);
    }


    @AfterEach
    public void restoreLotjuClient() {
        final LotjuTmsStationMetadataClient lotjuClient = getTargetObject(lotjuTmsStationMetadataClient);
        lotjuClient.setDestinationProvider(originalDestinationProvider);
    }

    @Test
    public void testUpdateTmsStations() {
        lotjuLAMMetatiedotServiceMock.initStateAndService();

        // Update TMS stations to initial state (3 non obsolete stations and 1 obsolete)
        tmsSensorUpdater.updateTmsSensors();
        tmsStationUpdater.updateTmsStations();
        tmsStationsSensorsUpdater.updateTmsStationsSensors();

        final long naturalIdToCheck1 = 23826;
        final long naturalIdToCheck2 = 23001;


        final TmsStationFeatureCollectionSimpleV1 allStationsBefore =
            tmsStationMetadataWebServiceV1.findAllPublishableTmsStationsAsSimpleFeatureCollection(false, ACTIVE);
        final TmsRoadStationsSensorsMetadata allSensorsBefore =
            roadStationSensorService.findTmsRoadStationsSensorsMetadata(false);
        final TmsStationFeatureDetailedV1 station1Before = tmsStationMetadataWebServiceV1.getTmsStationById(naturalIdToCheck1);
        final TmsStationFeatureDetailedV1 station2Before = tmsStationMetadataWebServiceV1.getTmsStationById(naturalIdToCheck2);

        assertEquals(3, allStationsBefore.getFeatures().size());


        // Now change lotju metadata and update tms stations (2 non obsolete stations and 2 obsolete)
        lotjuLAMMetatiedotServiceMock.setStateAfterChange(true);
        tmsSensorUpdater.updateTmsSensors();
        tmsStationUpdater.updateTmsStations();
        tmsStationsSensorsUpdater.updateTmsStationsSensors();

        final TmsStationFeatureCollectionSimpleV1 allStationsAfterChange =
            tmsStationMetadataWebServiceV1.findAllPublishableTmsStationsAsSimpleFeatureCollection(false, ACTIVE);
        final TmsRoadStationsSensorsMetadata allSensorsAfterChange =
            roadStationSensorService.findTmsRoadStationsSensorsMetadata(false);
        final TmsStationFeatureDetailedV1 station1After =
            tmsStationMetadataWebServiceV1.getTmsStationById(naturalIdToCheck1);
        final TmsStationFeatureDetailedV1 station2After =
            tmsStationMetadataWebServiceV1.getTmsStationById(naturalIdToCheck2);
        assertEquals(2, allStationsAfterChange.getFeatures().size());

        assertNotNull(findWithNaturalId(allStationsBefore, 23001));
        assertNull(findWithNaturalId(allStationsBefore, 23002));
        assertNotNull(findWithNaturalId(allStationsBefore, 23826));
        assertNotNull(findWithNaturalId(allStationsBefore, 23005));

        assertNotNull(findWithNaturalId(allStationsAfterChange, 23001));
        assertNull(findWithNaturalId(allStationsAfterChange, 23002));
        assertNotNull(findWithNaturalId(allStationsAfterChange, 23826));
        assertNull(findWithNaturalId(allStationsAfterChange, 23005));

        assertEquals(CollectionStatus.GATHERING, findWithNaturalId(allStationsBefore, 23001).getProperties().getCollectionStatus());
        assertEquals(CollectionStatus.GATHERING, findWithNaturalId(allStationsBefore, 23826).getProperties().getCollectionStatus());
        assertEquals(CollectionStatus.REMOVED_TEMPORARILY, findWithNaturalId(allStationsBefore, 23005).getProperties().getCollectionStatus());

        assertEquals(CollectionStatus.GATHERING, findWithNaturalId(allStationsAfterChange, 23001).getProperties().getCollectionStatus());
        assertEquals(CollectionStatus.GATHERING, findWithNaturalId(allStationsAfterChange, 23826).getProperties().getCollectionStatus());


        assertEquals("vt5_Iisalmi", station1Before.getProperties().getName());
        assertEquals("vt5_Iidensalmi", station1After.getProperties().getName());

        // For conversions https://www.retkikartta.fi/
        final Point geomBefore = station1Before.getGeometry();
        final Point geomAfter = station1After.getGeometry();

        assertEquals(27.251752, geomBefore.getLongitude(), 0.000001);
        assertEquals(27.467862, geomAfter.getLongitude(), 0.000001);

        assertEquals(63.566830, geomBefore.getLatitude(), 0.000001);
        assertEquals(64.463708, geomAfter.getLatitude(), 0.000001);

        assertEquals(0.0, geomBefore.getAltitude(), 0.00005);
        assertEquals(1.0, geomAfter.getAltitude(), 0.00005);

        assertEquals(4750, station1Before.getProperties().getRoadAddress().distanceFromRoadSectionStart);
        assertEquals(4751, station1After.getProperties().getRoadAddress().distanceFromRoadSectionStart);

        assertEquals(300, station1Before.getProperties().getCollectionInterval());
        assertEquals(301, station1After.getProperties().getCollectionInterval());

        assertEquals("Pohjois-Savo", station1Before.getProperties().getProvince());
        assertEquals("Pohjois-Savvoo", station1After.getProperties().getProvince());

        assertEquals("Tie 5 Iisalmi", station1Before.getProperties().getNames().get("fi"));
        assertEquals("Tie 5 Idensalmi", station1After.getProperties().getNames().get("fi"));

        assertEquals("Väg 5 Idensalmi", station1Before.getProperties().getNames().get("sv"));
        assertEquals("Väg 5 Idensalmi", station1After.getProperties().getNames().get("sv"));

        assertEquals("Road 5 Iisalmi", station1Before.getProperties().getNames().get("en"));
        assertEquals("Road 5 Idensalmi", station1After.getProperties().getNames().get("en"));

        assertEquals("Kajaani", station1Before.getProperties().direction1Municipality);
        assertEquals("Kajaaniin", station1After.getProperties().direction1Municipality);

        assertEquals("Kuopio", station1Before.getProperties().direction2Municipality);
        assertEquals("Kuopioon", station1After.getProperties().direction2Municipality);


        final List<Long> station2SensorsBefore = station2Before.getProperties().sensors;
        final List<Long> station2SensorsAfter = station2After.getProperties().sensors;

        assertTrue(station2SensorsBefore.contains(5116L));
        assertTrue(station2SensorsBefore.contains(5119L));
        assertTrue(station2SensorsBefore.contains(5122L));
        assertFalse(station2SensorsBefore.contains(5125L));

        assertTrue(station2SensorsAfter.contains(5116L));
        assertFalse(station2SensorsAfter.contains(5119L)); // public false
        assertFalse(station2SensorsAfter.contains(5122L));
        assertTrue(station2SensorsAfter.contains(5125L));

        TmsRoadStationSensorDto initialSensor = allSensorsBefore.getRoadStationSensors().stream().filter(x -> x.getNaturalId() == 5116L).findFirst().orElse(null);
        TmsRoadStationSensorDto afterChangeSensor = allSensorsAfterChange.getRoadStationSensors().stream().filter(x -> x.getNaturalId() == 5116L).findFirst().orElse(null);
        assertNull(initialSensor.getDirection());
        assertNull(initialSensor.getLane());
        assertNull(initialSensor.getVehicleClass());

        assertEquals(1, afterChangeSensor.getDirection().intValue());
        assertEquals(2, afterChangeSensor.getLane().intValue());
        assertEquals(VehicleClass.TRUCK, afterChangeSensor.getVehicleClass());
    }

    private TmsStationFeatureSimpleV1 findWithNaturalId(final TmsStationFeatureCollectionSimpleV1 collection, final long naturalId) {
        final Optional<TmsStationFeatureSimpleV1> initial =
                collection.getFeatures().stream()
                        .filter(x -> x.getProperties().id == naturalId)
                        .findFirst();
        return initial.orElse(null);
    }
}
