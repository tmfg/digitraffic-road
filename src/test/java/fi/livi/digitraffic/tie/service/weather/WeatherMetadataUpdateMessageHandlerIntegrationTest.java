package fi.livi.digitraffic.tie.service.weather;

import static fi.livi.digitraffic.tie.TestUtils.createTiesaaAsema;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.ROAD_ADDRESS;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_COMPUTATIONAL_SENSOR;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_STATION;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_STATION_COMPUTATIONAL_SENSOR;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.dao.weather.WeatherStationRepository;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TieosoiteVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.lotju.AbstractMetadataUpdateMessageHandlerIntegrationTest;

public class WeatherMetadataUpdateMessageHandlerIntegrationTest extends AbstractMetadataUpdateMessageHandlerIntegrationTest {

    @Autowired
    private WeatherMetadataUpdateMessageHandler weatherMetadataUpdateMessageHandler;

    @Autowired
    private WeatherStationRepository weatherStationRepository;

    @Autowired
    private StationSensorConverterService stationSensorConverterService;

    @Autowired
    private WeatherStationService weatherStationService;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private RoadStationSensorRepository roadStationSensorRepository;

    @AfterEach
    protected void clearDb() {
        TestUtils.truncateWeatherData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    private static final long ROAD_STATION_LOTJU_ID = 1L;
    private static final Pair<Long, Long> ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1 = Pair.of(1L,1L); // ILMA
    private static final Pair<Long, Long> ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2 = Pair.of(2L,2L); // ILMA_DERIVAATTA

    @AfterEach
    public void cleanDb() {
        TestUtils.truncateWeatherData(entityManager);
    }

    @Test
    public void weatherStationInsertMessage() {
        // 1. There is no data for station
        assertNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));

        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(createTiesaaAsema(ROAD_STATION_LOTJU_ID));
        final List<TiesaaLaskennallinenAnturiVO> anturit = createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);

        // 2. Send insert message
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(WEATHER_STATION, UpdateType.INSERT, ROAD_STATION_LOTJU_ID, ROAD_STATION_LOTJU_ID));
        TestUtils.flushCommitEndTransactionAndStartNew(entityManager);
        // 3. Check that new station and sensor are in place
        final RoadStation after = getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        assertEquals(ROAD_STATION_LOTJU_ID, requireNonNull(after).getLotjuId());
        assertTrue(after.isPublishable());
        final Map<Long, List<Long>> allSensorsAfterInsert =
            stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(after.getId(), RoadStationType.WEATHER_STATION);
        final List<Long> stationsSensors = allSensorsAfterInsert.get(after.getId());
        assertCollectionSize(1, stationsSensors);
        assertEquals(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1.getRight(), stationsSensors.get(0).longValue());
    }

    @Test
    public void weatherStationComputationalSensorInsertMessage() {
        // 1. There is no data for station
        assertNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));

        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(createTiesaaAsema(ROAD_STATION_LOTJU_ID));
        final List<TiesaaLaskennallinenAnturiVO> anturit = createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);

        // 2. Send insert message
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(WEATHER_STATION_COMPUTATIONAL_SENSOR, UpdateType.INSERT, anturit.get(0).getId(), ROAD_STATION_LOTJU_ID));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Check that new station and sensor are in place
        final RoadStation after = getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        assertEquals(ROAD_STATION_LOTJU_ID, requireNonNull(after).getLotjuId());
        assertTrue(after.isPublishable());
        final Map<Long, List<Long>> allSensorsAfterInsert =
            stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(after.getId(), RoadStationType.WEATHER_STATION);
        final List<Long> stationsSensors = allSensorsAfterInsert.get(after.getId());
        assertCollectionSize(1, stationsSensors);
        assertEquals(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1.getRight(), stationsSensors.get(0).longValue());

    }

    private void assertWeatherStationAndSensorInsertMessage(final EntityType entityType) {
        // Both have same inserting process
        assertTrue(WEATHER_STATION.equals(entityType) || WEATHER_STATION_COMPUTATIONAL_SENSOR.equals(entityType));

        // 1. There is no data for station
        assertNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));

        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(createTiesaaAsema(ROAD_STATION_LOTJU_ID));
        final List<TiesaaLaskennallinenAnturiVO> anturit = createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);

        // 2. Send insert message
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(entityType, UpdateType.INSERT, ROAD_STATION_LOTJU_ID, ROAD_STATION_LOTJU_ID));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Check that new station and sensor are in place
        final RoadStation after = getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        assertEquals(ROAD_STATION_LOTJU_ID, requireNonNull(after).getLotjuId());
        assertTrue(after.isPublishable());
        final Map<Long, List<Long>> allSensorsAfterInsert =
            stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(after.getId(), RoadStationType.WEATHER_STATION);
        final List<Long> stationsSensors = allSensorsAfterInsert.get(after.getId());
        assertCollectionSize(1, stationsSensors);
        assertEquals(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1.getRight(), stationsSensors.get(0).longValue());
    }

    private List<TiesaaLaskennallinenAnturiVO> createAnturiListWith(final Pair<Long, Long> sensorLotjuIdAndNaturalId) {
        final List<TiesaaLaskennallinenAnturiVO> anturit = TestUtils.createTiesaaLaskennallinenAnturis(1);
        anturit.get(0).setId(sensorLotjuIdAndNaturalId.getLeft());
        anturit.get(0).setVanhaId(sensorLotjuIdAndNaturalId.getRight().intValue());
        return anturit;
    }

    @Test
    public void weatherStationUpdateMessage() {
        assertWeatherStationAndSensorUpdateMessage(WEATHER_STATION);
    }

    @Test
    public void weatherStationComputationalSensorUpdateMessage() {
        assertWeatherStationAndSensorUpdateMessage(WEATHER_STATION_COMPUTATIONAL_SENSOR);
    }

    public void assertWeatherStationAndSensorUpdateMessage(final EntityType entityType) {
        // Both have same updating process
        assertTrue(WEATHER_STATION.equals(entityType) || WEATHER_STATION_COMPUTATIONAL_SENSOR.equals(entityType));

        // 1. generate station data before update message
        assertNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        final WeatherStation rws = weatherStationService.findWeatherStationByLotjuId(ROAD_STATION_LOTJU_ID);
        // Add sensor 1 for station and check it's saved in db
        roadStationSensorService.updateSensorsOfRoadStation(rws.getRoadStationId(),
                                                            RoadStationType.WEATHER_STATION,
                                                            Collections.singletonList(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1.getLeft()));

        // 2. send update message (name change and sensor 1 removed and sensor 2 added)
        tsa.setNimiEn("Changed name");
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(tsa);
        final List<TiesaaLaskennallinenAnturiVO> anturit = createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);

        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(entityType, UpdateType.UPDATE, 1, ROAD_STATION_LOTJU_ID));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Check that update is done
        final RoadStation rsAfterUpdate = getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        assertEquals(tsa.getNimiEn(), requireNonNull(rsAfterUpdate).getNameEn());

        final Map<Long, List<Long>> allSensorsAfterUpdate = stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(rsAfterUpdate.getId(), RoadStationType.WEATHER_STATION);
        final List<Long> roadStationsSensors = allSensorsAfterUpdate.get(rsAfterUpdate.getId());
        assertCollectionSize(1, roadStationsSensors);
        assertEquals(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2.getRight(), roadStationsSensors.get(0).longValue());
    }

    @Test
    public void weatherStationDeleteMessage() {
        // 1. Create station to delete
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        TestUtils.entityManagerFlushAndClear(entityManager);
        assertTrue(requireNonNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID)).isPublishable());

        // 2. Send delete message
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(WEATHER_STATION, UpdateType.DELETE, 1, 1L));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Assert delete happened
        assertFalse(requireNonNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID)).isPublishable());
    }

    @Test
    public void weatherStationComputationalSensorDeleteMessage() {
        // 1. generate station data with two sensors before delete message
        assertNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        final WeatherStation rws = weatherStationService.findWeatherStationByLotjuId(ROAD_STATION_LOTJU_ID);
        // Add sensor 1 and 2 for station
        roadStationSensorService.updateSensorsOfRoadStation(rws.getRoadStationId(),
            RoadStationType.WEATHER_STATION,
            Arrays.asList(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1.getLeft(), ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2.getLeft()));

        // 2. Send message to delete sensor 1
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(tsa);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID)))
            .thenReturn(createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2));
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(
            createMessage(WEATHER_STATION_COMPUTATIONAL_SENSOR, UpdateType.DELETE, ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_1.getLeft(), ROAD_STATION_LOTJU_ID));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Assert station is still public
        assertTrue(requireNonNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID)).isPublishable());
        // And sensor 1 is deleted
        final Map<Long, List<Long>> allSensorsAfterDelete = stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(rws.getRoadStationId(), RoadStationType.WEATHER_STATION);
        final List<Long> roadStationSensors = allSensorsAfterDelete.get(rws.getRoadStationId());
        assertCollectionSize(1, roadStationSensors);
        assertEquals(ALLOWED_SENSOR_LOTJU_ID_AND_NATURAL_ID_PAIR_2.getRight(), roadStationSensors.get(0).longValue());
    }

    @Test
    public void weatherComputationalSensorInsertMessage() {
        // 1. There is station without sensors
        final long NEW_LOTJU_ID = 999999L;
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        final int sensorsCountBeforeUpdate = getWeatherSensorsFromDb().size();

        // 2. Send insert message
        final TiesaaLaskennallinenAnturiVO anturi = createAnturiListWith(Pair.of(NEW_LOTJU_ID, NEW_LOTJU_ID)).get(0);
        when(lotjuWeatherStationMetadataClient.getTiesaaLaskennallinenAnturi(eq(NEW_LOTJU_ID))).thenReturn(anturi);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(tsa);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(Collections.singletonList(anturi));

        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(
            createMessage(WEATHER_COMPUTATIONAL_SENSOR, UpdateType.INSERT, NEW_LOTJU_ID, ROAD_STATION_LOTJU_ID));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor is place
        final List<RoadStationSensor> allSensorsAfterInsert = getWeatherSensorsFromDb();
        assertCollectionSize(sensorsCountBeforeUpdate+1, allSensorsAfterInsert);
        assertTrue(allSensorsAfterInsert.stream().anyMatch(s -> s.getLotjuId().equals(NEW_LOTJU_ID)));
        // And it is added to road station
        final long rwsId = requireNonNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID)).getId();
        final Map<Long, List<Long>> allSensorsMappedByRsId = stationSensorConverterService.getPublishableSensorsNaturalIdsMappedByRoadStationId(rwsId, RoadStationType.WEATHER_STATION);
        final List<Long> roadStationsSensors = allSensorsMappedByRsId.get(rwsId);
        assertCollectionSize(1, roadStationsSensors);
        assertEquals(NEW_LOTJU_ID, roadStationsSensors.get(0).longValue());
    }


    @Test
    public void weatherComputationalSensorUpdateMessage() {
        // 1. there is no sensor with new lotju id
        final int sensorsCountBeforeUpdate = getWeatherSensorsFromDb().size();

        final TiesaaLaskennallinenAnturiVO anturi = TestUtils.createTiesaaLaskennallinenAnturis(1).get(0);
        assertEquals(UpdateStatus.INSERTED, roadStationSensorService.updateOrInsert(anturi));

        // 2. Send update message
        final String newDesc = "Uusi kuvaus";
        anturi.setKuvausFi(newDesc);
        when(lotjuWeatherStationMetadataClient.getTiesaaLaskennallinenAnturi(eq(anturi.getId()))).thenReturn(anturi);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(createTiesaaAsema(ROAD_STATION_LOTJU_ID));
        final List<TiesaaLaskennallinenAnturiVO> anturit = createAnturiListWith(Pair.of(anturi.getId(), (long) anturi.getVanhaId()));
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(WEATHER_COMPUTATIONAL_SENSOR, UpdateType.UPDATE, anturi.getId(), ROAD_STATION_LOTJU_ID));

        // 3. Check that new sensor is place and also station has it
        final List<RoadStationSensor> allSensorsAfterUpdate = getWeatherSensorsFromDb();
        assertCollectionSize(sensorsCountBeforeUpdate+1, allSensorsAfterUpdate);
        final RoadStationSensor updatedSensor = allSensorsAfterUpdate.stream().filter(s -> s.getLotjuId().equals(anturi.getId())).findFirst().orElseThrow();
        assertEquals(newDesc, updatedSensor.getDescriptionFi());
        final WeatherStation ws = weatherStationRepository.findByLotjuId(ROAD_STATION_LOTJU_ID);
        final List<Long> sensors =
            roadStationSensorRepository.findRoadStationPublishableSensorsNaturalIdsByStationIdAndType(ws.getRoadStationId(), RoadStationType.WEATHER_STATION);
        assertTrue(sensors.contains((long) anturi.getVanhaId()));
    }

    @Test
    public void weatherComputationalSensorDeleteMessage() {
        // 1. there is no sensor with new lotju id
        final TiesaaLaskennallinenAnturiVO anturi = TestUtils.createTiesaaLaskennallinenAnturis(1).get(0);
        assertEquals(UpdateStatus.INSERTED, roadStationSensorService.updateOrInsert(anturi));

        // 2. Send update message
        when(lotjuWeatherStationMetadataClient.getTiesaaLaskennallinenAnturi(eq(anturi.getId()))).thenReturn(anturi);
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(WEATHER_COMPUTATIONAL_SENSOR, UpdateType.DELETE, anturi.getId(), ROAD_STATION_LOTJU_ID));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor is place
        final RoadStationSensor updatedSensor =
            getWeatherSensorsFromDb().stream().filter(s -> s.getLotjuId().equals(anturi.getId())).findFirst().orElseThrow();
        assertNotNull(updatedSensor.getObsoleteDate());
        assertFalse(updatedSensor.isPublishable());
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

    private void assertRoadAddressUpdateMessage(final UpdateType updateType) {
        // 1. Station with road address
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        final TieosoiteVO to = tsa.getTieosoite();

        // 2. Send road address insert message
        to.setUrakkaAlue(RandomStringUtils.randomAlphanumeric(32));
        to.setId(to.getId()+1);
        to.setTienumero(to.getTienumero()+1);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(tsa);
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(ROAD_ADDRESS, updateType, tsa.getId(), tsa.getId()));

        // 3. Check that road address is updated
        final RoadStation rs = getWeatherStationRoadStationByLotjuId(tsa.getId());
        assertEquals(to.getTienumero(), requireNonNull(rs).getRoadAddress().getRoadNumber());
        assertEquals(to.getUrakkaAlue(), rs.getRoadAddress().getContractArea());
    }

    private List<WeatherMetadataUpdatedMessageDto> createMessage(final EntityType entityType, final UpdateType updateType, final long lotjuId, final Long...asemaLotjuIds) {
        return Collections.singletonList(
            new WeatherMetadataUpdatedMessageDto(lotjuId, Set.of(asemaLotjuIds), updateType, Instant.now(), entityType));
    }

    private List<RoadStationSensor> getWeatherSensorsFromDb() {
        return roadStationSensorRepository.findAll().stream()
            .filter(s -> s.getRoadStationType().equals(RoadStationType.WEATHER_STATION))
            .collect(Collectors.toList());
    }

    private RoadStation getWeatherStationRoadStationByLotjuId(final long roadStationLotjuId) {
        final WeatherStation wrs = weatherStationRepository.findByLotjuId(roadStationLotjuId);
        return wrs != null ? wrs.getRoadStation() : null;
    }
}
