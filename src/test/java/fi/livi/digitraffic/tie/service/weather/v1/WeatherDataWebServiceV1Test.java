package fi.livi.digitraffic.tie.service.weather.v1;

import static fi.livi.digitraffic.tie.TestUtils.getRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.test.util.AssertUtil;
import fi.livi.digitraffic.tie.AbstractWebServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueRepository;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationDataDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationsDataDtoV1;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValue;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueReliability;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;

/**
 * Test for {@link WeatherDataWebServiceV1}
 */
public class WeatherDataWebServiceV1Test extends AbstractWebServiceTest {

    @Autowired
    private WeatherDataWebServiceV1 weatherDataWebServiceV1;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private SensorValueRepository sensorValueRepository;

    @Autowired
    private RoadStationSensorServiceV1 roadStationSensorService;

    private WeatherStation weatherStation;
    private SensorValue sensorValue1, sensorValue2;

    @BeforeEach
    public void updateData() {
        TestUtils.truncateWeatherData(entityManager);
        final List<RoadStationSensor> publishable =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);
        assertFalse(publishable.isEmpty());

        TestUtils.generateDummyWeatherStations(2).forEach(s -> {
            weatherStation = s;
            entityManager.persist(s);
            entityManager.flush();

            sensorValue1 = new SensorValue(s.getRoadStation(), publishable.getFirst(), getRandom(0, 100), Instant.now(), SensorValueReliability.OK);
            sensorValue2 = new SensorValue(s.getRoadStation(), publishable.get(1), getRandom(101, 200), Instant.now(), SensorValueReliability.OK);
            sensorValueRepository.save(sensorValue1);
            sensorValueRepository.save(sensorValue2);
        });

        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.WEATHER_STATION));
    }

    @AfterEach
    protected void cleanDb() {
        TestUtils.truncateWeatherData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    @Test
    public void findPublishableWeatherData() {
        final WeatherStationsDataDtoV1 stationsData = weatherDataWebServiceV1.findPublishableWeatherData(false);

        assertNotNull(stationsData);
        assertNotNull(stationsData.dataUpdatedTime);

        AssertUtil.assertCollectionSize(2, stationsData.stations);
        final WeatherStationDataDtoV1
            data = stationsData.stations.stream().filter(s -> s.id.equals(weatherStation.getRoadStationNaturalId())).findFirst().orElseThrow();
        AssertUtil.assertCollectionSize(2, data.sensorValues);

        final SensorValueDtoV1 sv1 =
            data.sensorValues.stream().filter(sv -> sensorValue1.getRoadStationSensor().getNaturalId() == sv.getSensorNaturalId()).findFirst()
                .orElseThrow();
        final SensorValueDtoV1 sv2 =
            data.sensorValues.stream().filter(sv -> sensorValue2.getRoadStationSensor().getNaturalId() == sv.getSensorNaturalId()).findFirst()
                .orElseThrow();

        assertEquals(sensorValue1.getRoadStation().getNaturalId(), sv1.getRoadStationNaturalId());
        assertEquals(sensorValue2.getRoadStation().getNaturalId(), sv2.getRoadStationNaturalId());
        assertEquals(sensorValue1.getValue(), sv1.getValue());
        assertEquals(sensorValue2.getValue(), sv2.getValue());
        assertEquals(sensorValue1.getRoadStationSensor().getNameFi(), sv1.getSensorNameFi());
        assertEquals(sensorValue2.getRoadStationSensor().getNameFi(), sv2.getSensorNameFi());
    }

    @Test
    public void findPublishableWeatherDataById() {
        final WeatherStationDataDtoV1 data = weatherDataWebServiceV1.findPublishableWeatherData(weatherStation.getRoadStationNaturalId());
        assertNotNull(data);
        assertNotNull(data.dataUpdatedTime);

        final SensorValueDtoV1 sv1 =
            data.sensorValues.stream().filter(sv -> sensorValue1.getRoadStationSensor().getNaturalId() == sv.getSensorNaturalId()).findFirst()
                .orElseThrow();
        final SensorValueDtoV1 sv2 =
            data.sensorValues.stream().filter(sv -> sensorValue2.getRoadStationSensor().getNaturalId() == sv.getSensorNaturalId()).findFirst()
                .orElseThrow();

        assertEquals(sensorValue1.getRoadStation().getNaturalId(), sv1.getRoadStationNaturalId());
        assertEquals(sensorValue2.getRoadStation().getNaturalId(), sv2.getRoadStationNaturalId());
        assertEquals(sensorValue1.getValue(), sv1.getValue());
        assertEquals(sensorValue2.getValue(), sv2.getValue());
        assertEquals(sensorValue1.getRoadStationSensor().getNameFi(), sv1.getSensorNameFi());
        assertEquals(sensorValue2.getRoadStationSensor().getNameFi(), sv2.getSensorNameFi());
    }

    @Test
    public void findPublishableWeatherDataByIdNotFound() {
        assertThrows(ObjectNotFoundException.class, () -> weatherDataWebServiceV1.findPublishableWeatherData(-1));
    }
}
