package fi.livi.digitraffic.tie.service.v1.tms;

import static fi.livi.digitraffic.tie.TestUtils.createLamAsema;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType.ROAD_ADDRESS;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType.TMS_COMPUTATIONAL_SENSOR;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType.TMS_SENSOR_CONSTANT;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType.TMS_SENSOR_CONSTANT_VALUE;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType.TMS_STATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.dao.v1.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.dao.v1.TmsSensorConstantValueDtoRepository;
import fi.livi.digitraffic.tie.dao.v1.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDto;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.TieosoiteVO;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.v1.AbstractMetadataUpdateMessageHandlerIntegrationTest;

public class TmsMetadataUpdateMessageHandlerIntegrationTest extends AbstractMetadataUpdateMessageHandlerIntegrationTest {

    @Autowired
    private TmsMetadataUpdateMessageHandler tmsMetadataUpdateMessageHandler;

    @Autowired
    private TmsStationRepository tmsStationRepository;

    @Autowired
    private StationSensorConverterService stationSensorConverterService;

    @Autowired
    private TmsStationService tmsStationService;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private RoadStationSensorRepository roadStationSensorRepository;

    @Autowired
    private TmsStationSensorConstantService tmsStationSensorConstantService;

    @Autowired
    private TmsSensorConstantValueDtoRepository tmsSensorConstantValueDtoRepository;

    @AfterEach
    protected void clearDb() {
        TestUtils.truncateTmsData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    private static final long ROAD_STATION_LOTJU_ID = 1L;
    private static final Pair<Long, Long> ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1 = Pair.of(117L,5054L); // OHITUKSET_60MIN_KIINTEA_SUUNTA1
    private static final Pair<Long, Long> ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2 = Pair.of(118L,5055L); // OHITUKSET_60MIN_KIINTEA_SUUNTA2
    private static final String SENSOR_CONSTANT_NAME_1 = "VVAPAAS1";
    private static final String SENSOR_CONSTANT_NAME_2 = "VVAPAAS2";

    /**
     *         OK - TMS_STATION("LAM_ASEMA"),
     *         EI - TMS_SENSOR("LAM_ANTURI"),
     *         OK TMS_COMPUTATIONAL_SENSOR("LAM_LASKENNALLINENANTURI"),
     *         TMS_SENSOR_CONSTANT("LAM_ANTURIVAKIO"),
     *         TMS_SENSOR_CONSTANT_VALUE("LAM_ANTURIVAKIO_ARVO"),
     *         OK - ROAD_ADDRESS("TIEOSOITE");
     */

    @Test
    public void tmsStationInsertMessage() {
        // 1. There is no data for station
        assertNull(getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));

        when(lotjuTmsStationMetadataClient.getLamAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(createLamAsema(ROAD_STATION_LOTJU_ID));
        final List<LamLaskennallinenAnturiVO> anturit = createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1);
        when(lotjuTmsStationMetadataClient.getLamAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);

        // 2. Send insert message
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(TMS_STATION, UpdateType.INSERT, ROAD_STATION_LOTJU_ID, Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Check that new station and sensor are in place
        final RoadStation rsAfterInsert = getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        assertEquals(ROAD_STATION_LOTJU_ID, rsAfterInsert.getLotjuId());
        assertTrue(rsAfterInsert.isPublishable());
        final Map<Long, List<Long>> rsSensorsNaturalIdsAfterInsert =
            stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(rsAfterInsert.getId(), RoadStationType.TMS_STATION);
        final List<Long> stationsSensors = rsSensorsNaturalIdsAfterInsert.get(rsAfterInsert.getId());
        AssertHelper.assertCollectionSize(1, stationsSensors);
        final long addedSensorNaturalId = stationsSensors.get(0).longValue();
        assertEquals(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1.getRight(), addedSensorNaturalId);
    }

    @Test
    public void tmsStationUpdateMessage() {
        // 1. generate station data before update message
        assertNull(getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));
        final LamAsemaVO lam = createLamAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, tmsStationService.updateOrInsertTmsStation(lam));
        final TmsStation rws = tmsStationService.findTmsStationByLotjuId(ROAD_STATION_LOTJU_ID);
        // Add sensor 1 for station
        roadStationSensorService.updateSensorsOfRoadStation(rws.getRoadStationId(),
            RoadStationType.TMS_STATION,
            Collections.singletonList(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1.getLeft()));

        // 2. send update message (name change and sensor 1 removed and sensor 2 added)
        lam.setNimiEn("Changed name");
        when(lotjuTmsStationMetadataClient.getLamAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(lam);
        final List<LamLaskennallinenAnturiVO> anturit = createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2);
        when(lotjuTmsStationMetadataClient.getLamAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);

        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(TMS_STATION, UpdateType.UPDATE, 1, Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Check that update is done
        final RoadStation rsAfterUpdate = getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        assertEquals(lam.getNimiEn(), rsAfterUpdate.getNameEn());

        // Roadstations sensors mapped by road stations' ids
        final Map<Long, List<Long>> rsSensorsNaturalIdsAfterUpdate =
            stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(rsAfterUpdate.getId(), RoadStationType.TMS_STATION);
        final List<Long> stationsSensors = rsSensorsNaturalIdsAfterUpdate.get(rsAfterUpdate.getId());
        AssertHelper.assertCollectionSize(1, stationsSensors);
        final long addedSensorNaturalId = stationsSensors.get(0).longValue();
        assertEquals(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2.getRight(), addedSensorNaturalId);
    }

    @Test
    public void tmsStationDeleteMessage() {
        // 1. Create station to delete
        final LamAsemaVO lam = createLamAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, tmsStationService.updateOrInsertTmsStation(lam));
        TestUtils.entityManagerFlushAndClear(entityManager);
        assertTrue(getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID).isPublishable());

        // 2. Send delete message
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(TMS_STATION, UpdateType.DELETE, 1, Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Assert delete happened
        assertFalse(getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID).isPublishable());
    }

    @Test
    public void tmsComputationalSensorInsertMessage() {
        // 1. There is station without sensors
        final long NEW_LOTJU_ID = 999999L;
        final LamAsemaVO lam = createLamAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, tmsStationService.updateOrInsertTmsStation(lam));
        final int sensorsCountBeforeUpdate = getTmsSensorsFromDb().size();
        TestUtils.addAllowedSensor(NEW_LOTJU_ID, RoadStationType.TMS_STATION, entityManager);

        // 2. Send insert message
        final LamLaskennallinenAnturiVO anturi = createAnturiListWith(Pair.of(NEW_LOTJU_ID, NEW_LOTJU_ID)).get(0);
        when(lotjuTmsStationMetadataClient.getLamLaskennallinenAnturi(eq(NEW_LOTJU_ID))).thenReturn(anturi);
        when(lotjuTmsStationMetadataClient.getLamAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(lam);
        when(lotjuTmsStationMetadataClient.getLamAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(Collections.singletonList(anturi));

        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(
            createMessage(TMS_COMPUTATIONAL_SENSOR, UpdateType.INSERT, NEW_LOTJU_ID, Collections.singleton(ROAD_STATION_LOTJU_ID)));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor is place
        final List<RoadStationSensor> allSensors = getTmsSensorsFromDb();
        AssertHelper.assertCollectionSize(sensorsCountBeforeUpdate+1, allSensors);
        assertTrue(allSensors.stream().filter(s -> s.getLotjuId().equals(NEW_LOTJU_ID)).findFirst().isPresent());
        // And it is added to road station
        final long tmsId = getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID).getId();
        final Map<Long, List<Long>> rsSensorsNaturalIdsAfterUpdate = stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(tmsId, RoadStationType.TMS_STATION);
        final List<Long> roadStationsSensors = rsSensorsNaturalIdsAfterUpdate.get(tmsId);
        AssertHelper.assertCollectionSize(1, roadStationsSensors);
        assertEquals(NEW_LOTJU_ID, roadStationsSensors.get(0).longValue());
    }


    @Test
    public void tmsComputationalSensorUpdateMessage() {
        // 1. there is no sensor with new lotju id
        final int sensorsCountBeforeUpdate = getTmsSensorsFromDb().size();

        final LamLaskennallinenAnturiVO anturi = TestUtils.createLamLaskennallinenAnturis(1).get(0);
        assertEquals(UpdateStatus.INSERTED, roadStationSensorService.updateOrInsert(anturi));

        // 2. Send update message
        final String newDesc = "Uusi kuvaus";
        anturi.setKuvausFi(newDesc);
        when(lotjuTmsStationMetadataClient.getLamLaskennallinenAnturi(eq(anturi.getId()))).thenReturn(anturi);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(TMS_COMPUTATIONAL_SENSOR, UpdateType.UPDATE, anturi.getId(), Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor is place
        final List<RoadStationSensor> sensors = getTmsSensorsFromDb();
        AssertHelper.assertCollectionSize(sensorsCountBeforeUpdate+1, sensors);
        final RoadStationSensor updatedSensor = sensors.stream().filter(s -> s.getLotjuId().equals(anturi.getId())).findFirst().orElseThrow();
        assertEquals(newDesc, updatedSensor.getDescriptionFi());
    }

    @Test
    public void tmsComputationalSensorDeleteMessage() {
        // 1. there is no sensor with new lotju id
        final LamLaskennallinenAnturiVO anturi = TestUtils.createLamLaskennallinenAnturis(1).get(0);
        assertEquals(UpdateStatus.INSERTED, roadStationSensorService.updateOrInsert(anturi));

        // 2. Send update message
        when(lotjuTmsStationMetadataClient.getLamLaskennallinenAnturi(eq(anturi.getId()))).thenReturn(anturi);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(TMS_COMPUTATIONAL_SENSOR, UpdateType.DELETE, anturi.getId(), Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor is place
        final RoadStationSensor updatedSensor =
            getTmsSensorsFromDb().stream().filter(s -> s.getLotjuId().equals(anturi.getId())).findFirst().orElseThrow();
        assertNotNull(updatedSensor.getObsoleteDate());
        assertFalse(updatedSensor.isPublishable());
    }

    @Test
    public void tmsSensorConstantInsertMessage() {
        // 1. There is station without sensors
        addTmsStationWithOutSensorConstant(ROAD_STATION_LOTJU_ID);

        // 2. Send insert message
        final LamAnturiVakioVO anturiVakio = TestUtils.createLamAnturiVakio(ROAD_STATION_LOTJU_ID, SENSOR_CONSTANT_NAME_1);
        when(lotjuTmsStationMetadataClient.getLamAnturiVakio(eq(anturiVakio.getId()))).thenReturn(anturiVakio);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(
            createMessage(TMS_SENSOR_CONSTANT, UpdateType.INSERT, anturiVakio.getId(), Collections.singleton(ROAD_STATION_LOTJU_ID)));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor constant is added to station
        final RoadStation tms = getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        final String sensorConstantName = getSensorConstantOfStation(tms.getId(), anturiVakio.getId());
        assertEquals(SENSOR_CONSTANT_NAME_1, sensorConstantName);
    }

    @Test
    public void tmsSensorConstantUpdateMessage() {
        // 1. There is station with sensors
        final long anturiVakio1LotjuId = addTmsStationWithSensorConstant(ROAD_STATION_LOTJU_ID, SENSOR_CONSTANT_NAME_1);

        // 2. Send update message
        final LamAnturiVakioVO anturiVakio = TestUtils.createLamAnturiVakio(ROAD_STATION_LOTJU_ID, SENSOR_CONSTANT_NAME_2);
        anturiVakio.setId(anturiVakio1LotjuId);
        when(lotjuTmsStationMetadataClient.getLamAnturiVakio(eq(anturiVakio1LotjuId))).thenReturn(anturiVakio);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(
            createMessage(TMS_SENSOR_CONSTANT, UpdateType.UPDATE,anturiVakio1LotjuId, Collections.singleton(ROAD_STATION_LOTJU_ID)));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that sensor constant name is updated
        final RoadStation tms = getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        assertEquals(SENSOR_CONSTANT_NAME_2, getSensorConstantOfStation(tms.getId(), anturiVakio1LotjuId));
    }

    @Test
    public void tmsSensorConstantDeleteMessage() {
        // 1. There is station with sensors
        final long anturiVakio1LotjuId = addTmsStationWithSensorConstant(ROAD_STATION_LOTJU_ID, SENSOR_CONSTANT_NAME_1);

        // 2. Send delete message
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(
            createMessage(TMS_SENSOR_CONSTANT, UpdateType.DELETE, anturiVakio1LotjuId, Collections.singleton(ROAD_STATION_LOTJU_ID)));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that sensor constant is removed from station
        final RoadStation tms = getTmsStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        assertNull(getSensorConstantOfStation(tms.getId(), anturiVakio1LotjuId));
    }


    @Test
    public void tmsSensorConstantValueInsertMessage() {
        // 1. There is station without sensor constant value
        final long anturiVakio1LotjuId = addTmsStationWithSensorConstant(ROAD_STATION_LOTJU_ID, SENSOR_CONSTANT_NAME_1);

        // 2. Send insert message
        final LamAnturiVakioArvoVO anturiVakioArvo = TestUtils.createLamAnturiVakioArvo(anturiVakio1LotjuId, 101, 1231, 95);
        mockLotjuTmsStationMetadataClientEveryMonthForAnturiVakioArvo(anturiVakioArvo);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(
            createMessage(TMS_SENSOR_CONSTANT_VALUE, UpdateType.INSERT, anturiVakioArvo.getId(), Collections.singleton(ROAD_STATION_LOTJU_ID)));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor constant value is added to station
        final List<TmsSensorConstantValueDto> allValues =
            tmsSensorConstantValueDtoRepository.findAllPublishableSensorConstantValues();
        AssertHelper.assertCollectionSize(1, allValues);
        final TmsSensorConstantValueDto value = allValues.get(0);
        Assertions.assertEquals(95, value.getValue());
        Assertions.assertEquals(SENSOR_CONSTANT_NAME_1, value.getName());
    }

    @Test
    public void tmsSensorConstantValueUpdateMessage() {
        // 1. There is station with sensor constant value
        final long anturiVakio1LotjuId = addTmsStationWithSensorConstant(ROAD_STATION_LOTJU_ID, SENSOR_CONSTANT_NAME_1);
        final LamAnturiVakioArvoVO anturiVakioArvo = TestUtils.createLamAnturiVakioArvo(anturiVakio1LotjuId, 101, 1231, 95);
        tmsStationSensorConstantService.updateSensorConstantValues(Collections.singletonList(anturiVakioArvo));

        // 2. Send update message
        anturiVakioArvo.setArvo(80);
        mockLotjuTmsStationMetadataClientEveryMonthForAnturiVakioArvo(anturiVakioArvo);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(
            createMessage(TMS_SENSOR_CONSTANT_VALUE, UpdateType.UPDATE, anturiVakioArvo.getId(), Collections.singleton(ROAD_STATION_LOTJU_ID)));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor constant value is updated
        final List<TmsSensorConstantValueDto> allValues =
            tmsSensorConstantValueDtoRepository.findAllPublishableSensorConstantValues();
        AssertHelper.assertCollectionSize(1, allValues);
        final TmsSensorConstantValueDto value = allValues.get(0);
        Assertions.assertEquals(80, value.getValue());
        Assertions.assertEquals(SENSOR_CONSTANT_NAME_1, value.getName());
    }

    @Test
    public void tmsSensorConstantValueDeleteMessage() {
        // 1. There is station with sensor constant value
        final long anturiVakio1LotjuId = addTmsStationWithSensorConstant(ROAD_STATION_LOTJU_ID, SENSOR_CONSTANT_NAME_1);
        final LamAnturiVakioArvoVO anturiVakioArvo = TestUtils.createLamAnturiVakioArvo(anturiVakio1LotjuId, 101, 1231, 95);
        tmsStationSensorConstantService.updateSensorConstantValues(Collections.singletonList(anturiVakioArvo));

        // 2. Send delete message
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(
            createMessage(TMS_SENSOR_CONSTANT_VALUE, UpdateType.DELETE, anturiVakioArvo.getId(), Collections.singleton(ROAD_STATION_LOTJU_ID)));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that deleted sensor constant value is removed from station
        final List<TmsSensorConstantValueDto> allValues =
            tmsSensorConstantValueDtoRepository.findAllPublishableSensorConstantValues();
        AssertHelper.assertCollectionSize(0, allValues);
    }

    @Test
    public void roadAddressInsertMessage() {
        assertRoadAddressUpdateMessage(UpdateType.INSERT);
    }

    @Test
    public void roadAddressUpdateMessage() {
        assertRoadAddressUpdateMessage(UpdateType.UPDATE);
    }

    @Test
    public void roadAddressDeleteMessage() {
        assertRoadAddressUpdateMessage(UpdateType.DELETE);
    }

    private void mockLotjuTmsStationMetadataClientEveryMonthForAnturiVakioArvo(final LamAnturiVakioArvoVO anturiVakioArvo) {
        IntStream.range(1,13).forEach(month ->
            when(lotjuTmsStationMetadataClient.getAnturiVakioArvot(anturiVakioArvo.getId(), month, 1)).thenReturn(anturiVakioArvo));
    }

    private void addTmsStationWithOutSensorConstant(final long stationLotjuId) {
        addTmsStationWithSensorConstant(stationLotjuId, null);
    }

    /**
     *
     * @param stationLotjuId station lotju id
     * @param sensorConstantName constant name
     * @return sensorConstantLotjuId
     */
    private Long addTmsStationWithSensorConstant(final long stationLotjuId, final String sensorConstantName) {
        final LamAsemaVO lam = createLamAsema(stationLotjuId);
        assertEquals(UpdateStatus.INSERTED, tmsStationService.updateOrInsertTmsStation(lam));
        TestUtils.entityManagerFlushAndClear(entityManager);
        if (sensorConstantName != null) {
            final LamAnturiVakioVO anturiVakio = TestUtils.createLamAnturiVakio(stationLotjuId, sensorConstantName);
            tmsStationSensorConstantService.updateSensorConstant(anturiVakio);
            return anturiVakio.getId();
        }
        return null;
    }

    private String getSensorConstantOfStation(final Long roadStationId, final Long sensorConstantLotjuId) {
        final List<String> result = entityManager.createNativeQuery(
            "SELECT NAME " +
            "FROM TMS_SENSOR_CONSTANT " +
            "WHERE road_station_id = " + roadStationId +
            "  AND lotju_id = " + sensorConstantLotjuId +
            "  AND obsolete_date is null").getResultList();
        Assertions.assertTrue(result.size() <= 1);
        return result.isEmpty() ? null : result.get(0);
    }

    private void assertRoadAddressUpdateMessage(final UpdateType updateType) {
        // 1. Station with road address
        final LamAsemaVO lam = createLamAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, tmsStationService.updateOrInsertTmsStation(lam));
        final TieosoiteVO to = lam.getTieosoite();

        // 2. Send road address insert message
        to.setUrakkaAlue(RandomStringUtils.randomAlphanumeric(32));
        to.setId(to.getId()+1);
        to.setTienumero(to.getTienumero()+1);
        when(lotjuTmsStationMetadataClient.getLamAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(lam);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(ROAD_ADDRESS, updateType, lam.getId(), Collections.singleton(lam.getId())));

        // 3. Check that road address is updated
        final RoadStation rs = getTmsStationRoadStationByLotjuId(lam.getId());
        assertEquals(to.getTienumero(), rs.getRoadAddress().getRoadNumber());
        assertEquals(to.getUrakkaAlue(), rs.getRoadAddress().getContractArea());
    }

    private List<TmsMetadataUpdatedMessageDto> createMessage(final EntityType entityType, final UpdateType updateType, long lotjuId, Set<Long> asemaLotjuIds) {
        return Collections.singletonList(
            new TmsMetadataUpdatedMessageDto(lotjuId, asemaLotjuIds, updateType, Instant.now(), entityType));
    }

    private List<RoadStationSensor> getTmsSensorsFromDb() {
        return roadStationSensorRepository.findAll().stream()
            .filter(s -> s.getRoadStationType().equals(RoadStationType.TMS_STATION))
            .collect(Collectors.toList());
    }

    private RoadStation getTmsStationRoadStationByLotjuId(final long roadStationLotjuId) {
        final TmsStation wrs = tmsStationRepository.findByLotjuId(roadStationLotjuId);
        return wrs != null ? wrs.getRoadStation() : null;
    }

    private List<LamLaskennallinenAnturiVO> createAnturiListWith(final Pair<Long, Long> sensorLotjuIdAndNaturalId) {
        final List<LamLaskennallinenAnturiVO> anturit = TestUtils.createLamLaskennallinenAnturis(1);
        anturit.get(0).setId(sensorLotjuIdAndNaturalId.getLeft());
        anturit.get(0).setVanhaId(sensorLotjuIdAndNaturalId.getRight().intValue());
        return anturit;
    }

}
