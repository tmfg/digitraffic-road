package fi.livi.digitraffic.tie.service.v1.weather;

import static fi.livi.digitraffic.tie.TestUtils.createTiesaaAsema;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_COMPUTATIONAL_SENSOR;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_STATION;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_STATION_COMPUTATIONAL_SENSOR;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.dao.v1.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.dao.v1.WeatherStationRepository;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TieosoiteVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuWeatherStationMetadataClient;

public class WeatherMetadataUpdateMessageHandlerIntegrationTest extends AbstractDaemonTest {

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

    @MockBean
    private LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;

    @AfterEach
    protected void clearDb() {
        TestUtils.truncateWeatherData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    private static final long ROAD_STATION_LOTJU_ID = 1L;
    private static final long ALLOWED_SENSOR_LOTJU_ID1 = 1L;
    private static final long ALLOWED_SENSOR_LOTJU_ID2 = 2L;
    private static final long NEW_SENSOR_LOTJU_ID = 999999L;


    @Test
    public void weatherStationInsertMessage() {
        assertWeatherStationAndSensorInsertMessage(WEATHER_STATION);
    }

    @Test
    public void weatherStationComputationalSensorInsertMessage() {
        assertWeatherStationAndSensorInsertMessage(WEATHER_STATION_COMPUTATIONAL_SENSOR);
    }

    private void assertWeatherStationAndSensorInsertMessage(final EntityType entityType) {
        // Both have same inserting process
        Assertions.assertTrue(WEATHER_STATION.equals(entityType) || WEATHER_STATION_COMPUTATIONAL_SENSOR.equals(entityType));

        // 1. There is no data for station
        Assertions.assertNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));

        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(createTiesaaAsema(ROAD_STATION_LOTJU_ID));
        final List<TiesaaLaskennallinenAnturiVO> anturit = createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID1);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);

        // 2. Send insert message
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(entityType, UpdateType.INSERT, ROAD_STATION_LOTJU_ID, Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Check that new station and sensor are in place
        final RoadStation after = getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        Assertions.assertEquals(ROAD_STATION_LOTJU_ID, after.getLotjuId());
        Assertions.assertTrue(after.isPublishable());
        final Map<Long, List<Long>> sensors = stationSensorConverterService.getPublishableSensorMap(after.getId(), RoadStationType.WEATHER_STATION);
        AssertHelper.assertCollectionSize(1, sensors.get(after.getId()));
        Assertions.assertEquals(ALLOWED_SENSOR_LOTJU_ID1, sensors.get(after.getId()).get(0).longValue());
    }

    private List<TiesaaLaskennallinenAnturiVO> createAnturiListWith(final long sensorLotjuId) {
        final List<TiesaaLaskennallinenAnturiVO> anturit = TestUtils.createTiesaaAsemaAnturit(1);
        anturit.get(0).setId(sensorLotjuId);
        anturit.get(0).setVanhaId((int) sensorLotjuId);
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
        Assertions.assertTrue(WEATHER_STATION.equals(entityType) || WEATHER_STATION_COMPUTATIONAL_SENSOR.equals(entityType));

        // 1. generate station data before update message
        Assertions.assertNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        Assertions.assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        final WeatherStation rws = weatherStationService.findWeatherStationByLotjuId(ROAD_STATION_LOTJU_ID);
        // Add sensor 1 for station and check it's saved in db
        roadStationSensorService.updateSensorsOfRoadStation(rws.getRoadStationId(),
                                                            RoadStationType.WEATHER_STATION,
                                                            Collections.singletonList(ALLOWED_SENSOR_LOTJU_ID1));

        // 2. send update message (name change and sensor 1 removed and sensor 2 added)
        tsa.setNimiEn("Changed name");
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(tsa);
        final List<TiesaaLaskennallinenAnturiVO> anturit = createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID2);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(anturit);

        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(entityType, UpdateType.UPDATE, 1, Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Check that update is done
        final RoadStation after = getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID);
        Assertions.assertEquals(tsa.getNimiEn(), after.getNameEn());

        final Map<Long, List<Long>> sensors = stationSensorConverterService.getPublishableSensorMap(after.getId(), RoadStationType.WEATHER_STATION);
        AssertHelper.assertCollectionSize(1, sensors.get(after.getId()));
        Assertions.assertEquals(ALLOWED_SENSOR_LOTJU_ID2, sensors.get(after.getId()).get(0).longValue());
    }

    @Test
    public void weatherStationDeleteMessage() {
        // 1. Create station to delete
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        Assertions.assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        TestUtils.entityManagerFlushAndClear(entityManager);
        Assertions.assertTrue(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID).isPublishable());

        // 2. Sen detete message
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(WEATHER_STATION, UpdateType.DELETE, 1, Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Assert delete happened
        Assertions.assertFalse(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID).isPublishable());
    }

    @Test
    public void weatherStationComputationalSensorDeleteMessage() {
        // 1. generate station data with two sensors before delete message
        Assertions.assertNull(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID));
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        Assertions.assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        final WeatherStation rws = weatherStationService.findWeatherStationByLotjuId(ROAD_STATION_LOTJU_ID);
        // Add sensor 1 and 2 for station
        roadStationSensorService.updateSensorsOfRoadStation(rws.getRoadStationId(),
            RoadStationType.WEATHER_STATION,
            Arrays.asList(ALLOWED_SENSOR_LOTJU_ID1, ALLOWED_SENSOR_LOTJU_ID2));

        // 2. Send message to delete sensor 1
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(tsa);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(createAnturiListWith(ALLOWED_SENSOR_LOTJU_ID2));
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(WEATHER_STATION_COMPUTATIONAL_SENSOR, UpdateType.DELETE, ALLOWED_SENSOR_LOTJU_ID1, Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        // 3. Assert station is still public
        Assertions.assertTrue(getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID).isPublishable());
        // And sensor 1 is deleted
        final Map<Long, List<Long>> sensors = stationSensorConverterService.getPublishableSensorMap(rws.getRoadStationId(), RoadStationType.WEATHER_STATION);
        AssertHelper.assertCollectionSize(1, sensors.get(rws.getRoadStationId()));
        Assertions.assertEquals(ALLOWED_SENSOR_LOTJU_ID2, sensors.get(rws.getRoadStationId()).get(0).longValue());
    }

    @Test
    public void weatherComputationalSensorInsertMessage() {
        // 1. There is station without sensors
        final long NEW_LOTJU_ID = 999999L;
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        Assertions.assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        final int sensorsCountBeforeUpdate = getWeatherSensorsFromDb().size();
        TestUtils.addAllowedSensor(NEW_LOTJU_ID, RoadStationType.WEATHER_STATION, entityManager);

        // 2. Send insert message
        final TiesaaLaskennallinenAnturiVO anturi = createAnturiListWith(NEW_LOTJU_ID).get(0);
        when(lotjuWeatherStationMetadataClient.getTiesaaLaskennallinenAnturi(eq(NEW_LOTJU_ID))).thenReturn(anturi);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(tsa);
        when(lotjuWeatherStationMetadataClient.getTiesaaAsemanLaskennallisetAnturit(eq(ROAD_STATION_LOTJU_ID))).thenReturn(Collections.singletonList(anturi));

        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(
            createMessage(WEATHER_COMPUTATIONAL_SENSOR, UpdateType.INSERT, NEW_LOTJU_ID, Collections.singleton(ROAD_STATION_LOTJU_ID)));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor is place
        final List<RoadStationSensor> allSensors = getWeatherSensorsFromDb();
        AssertHelper.assertCollectionSize(sensorsCountBeforeUpdate+1, allSensors);
        Assertions.assertTrue(allSensors.stream().filter(s -> s.getLotjuId().equals(NEW_LOTJU_ID)).findFirst().isPresent());
        // And it is added to road station
        final long rwsId = getWeatherStationRoadStationByLotjuId(ROAD_STATION_LOTJU_ID).getId();
        final Map<Long, List<Long>> sensors = stationSensorConverterService.getPublishableSensorMap(rwsId, RoadStationType.WEATHER_STATION);
        AssertHelper.assertCollectionSize(1, sensors.get(rwsId));
        Assertions.assertEquals(NEW_LOTJU_ID, sensors.get(rwsId).get(0).longValue());
    }


    @Test
    public void weatherComputationalSensorUpdateMessage() {
        // 1. there is no sensor with new lotju id
        final int sensorsCountBeforeUpdate = getWeatherSensorsFromDb().size();

        final TiesaaLaskennallinenAnturiVO anturi = TestUtils.createTiesaaAsemaAnturit(1).get(0);
        Assertions.assertEquals(UpdateStatus.INSERTED, roadStationSensorService.updateOrInsert(anturi));

        // 2. Send update message
        final String newDesc = "Uusi kuvaus";
        anturi.setKuvausFi(newDesc);
        when(lotjuWeatherStationMetadataClient.getTiesaaLaskennallinenAnturi(eq(anturi.getId()))).thenReturn(anturi);
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(WEATHER_COMPUTATIONAL_SENSOR, UpdateType.UPDATE, anturi.getId(), Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor is place
        final List<RoadStationSensor> sensors = getWeatherSensorsFromDb();
        AssertHelper.assertCollectionSize(sensorsCountBeforeUpdate+1, sensors);
        final RoadStationSensor updatedSensor = sensors.stream().filter(s -> s.getLotjuId().equals(anturi.getId())).findFirst().orElseThrow();
        Assertions.assertEquals(newDesc, updatedSensor.getDescriptionFi());
    }

    @Test
    public void weatherComputationalSensorDeleteMessage() {
        // 1. there is no sensor with new lotju id
        final TiesaaLaskennallinenAnturiVO anturi = TestUtils.createTiesaaAsemaAnturit(1).get(0);
        Assertions.assertEquals(UpdateStatus.INSERTED, roadStationSensorService.updateOrInsert(anturi));

        // 2. Send update message
        when(lotjuWeatherStationMetadataClient.getTiesaaLaskennallinenAnturi(eq(anturi.getId()))).thenReturn(anturi);
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(WEATHER_COMPUTATIONAL_SENSOR, UpdateType.DELETE, anturi.getId(), Collections.emptySet()));
        TestUtils.entityManagerFlushAndClear(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();

        // 3. Check that new sensor is place
        final RoadStationSensor updatedSensor =
            getWeatherSensorsFromDb().stream().filter(s -> s.getLotjuId().equals(anturi.getId())).findFirst().orElseThrow();
        Assertions.assertNotNull(updatedSensor.getObsoleteDate());
        Assertions.assertFalse(updatedSensor.isPublishable());
    }

    @Test
    public void roadAddressInsertMessage() {
        // 1. Station with road address
        final TiesaaAsemaVO tsa = createTiesaaAsema(ROAD_STATION_LOTJU_ID);
        Assertions.assertEquals(UpdateStatus.INSERTED, weatherStationService.updateOrInsertWeatherStation(tsa));
        final TieosoiteVO to = tsa.getTieosoite();

        // 2. Sen road address insert message
        TestUtils.createTieo
        tsa.set
        when(lotjuWeatherStationMetadataClient.getTiesaaAsema(eq(ROAD_STATION_LOTJU_ID))).thenReturn(tsa);
    }

    @Test
    public void roadAddressUpdateMessage() {

    }

    @Test
    public void roadAddressDeleteMessage() {

    }

    private List<WeatherMetadataUpdatedMessageDto> createMessage(final EntityType entityType, final UpdateType updateType, long lotjuId, Set<Long> asemmaLotjuIds) {
        return Collections.singletonList(
            new WeatherMetadataUpdatedMessageDto(lotjuId, asemmaLotjuIds, updateType, Instant.now(), entityType));
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
