package fi.livi.digitraffic.tie.service.tms.v1;

import static fi.livi.digitraffic.tie.TestUtils.getRandom;
import static fi.livi.digitraffic.tie.TestUtils.getRandomId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractWebServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.v1.SensorValueRepository;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationDataDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationSensorConstantDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationsDataDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationsSensorConstantsDataDtoV1;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDto;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.SensorValue;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.TmsTestHelper;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;

/**
 * Test for {@link TmsDataWebServiceV1}
 */
public class TmsDataWebServiceV1Test extends AbstractWebServiceTest {

    @Autowired
    private TmsDataWebServiceV1 tmsDataWebServiceV1;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private SensorValueRepository sensorValueRepository;

    @Autowired
    private RoadStationSensorServiceV1 roadStationSensorService;

    @Autowired
    private TmsTestHelper tmsTestHelper;

    private TmsStation tmsStation;
    private SensorValue sensorValue1, sensorValue2;

    @BeforeEach
    public void updateData() {

        final List<RoadStationSensor> publishable =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);
        assertFalse(publishable.isEmpty());

        TestUtils.generateDummyTmsStations(2).forEach(s -> {
            tmsStation = s;
            entityManager.persist(s);
            entityManager.flush();

            sensorValue1 = new SensorValue(s.getRoadStation(), publishable.get(0), getRandom(0, 100), ZonedDateTime.now());
            sensorValue2 = new SensorValue(s.getRoadStation(), publishable.get(1), getRandom(101, 200), ZonedDateTime.now());
            sensorValueRepository.save(sensorValue1);
            sensorValueRepository.save(sensorValue2);
        });

        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.TMS_STATION));
    }

    @Test
    public void findPublishableTmsData() {
        final TmsStationsDataDtoV1 stationsData = tmsDataWebServiceV1.findPublishableTmsData(false);

        assertNotNull(stationsData);
        assertNotNull(stationsData.dataUpdatedTime);

        AssertHelper.assertCollectionSize(2, stationsData.stations);
        final TmsStationDataDtoV1 data = stationsData.stations.stream().filter(s -> s.id.equals(tmsStation.getRoadStationNaturalId())).findFirst().orElseThrow();
        AssertHelper.assertCollectionSize(2, data.sensorValues);

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
    public void findPublishableTmsDataById() {
        final TmsStationDataDtoV1 data = tmsDataWebServiceV1.findPublishableTmsData(tmsStation.getRoadStationNaturalId());
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
    public void findPublishableTmsDataByIdNotFound() {
        assertThrows(ObjectNotFoundException.class, () -> tmsDataWebServiceV1.findPublishableTmsData(-1));
    }

    @Test
    public void findPublishableSensorConstants() {
        final String vakioNimi = "VVAPAAS1";
        final Integer vakioArvo = getRandomId(0, 20);

        final LamAnturiVakioVO vakio = tmsTestHelper.createAndSaveLamAnturiVakio(tmsStation.getLotjuId(), vakioNimi);
        tmsTestHelper.createAndSaveLamAnturiVakioArvo(vakio, vakioArvo);

        final TmsStationsSensorConstantsDataDtoV1 result = tmsDataWebServiceV1.findPublishableSensorConstants(false);
        AssertHelper.assertCollectionSize(1, result.stations);
        AssertHelper.assertCollectionSize(1, result.stations.get(0).sensorConstanValues);
        final TmsSensorConstantValueDto scv = result.stations.get(0).sensorConstanValues.get(0);
        assertEquals(vakioNimi, scv.getName());
        assertEquals(vakioArvo, scv.getValue());
        assertEquals("01-01", scv.getValidFromFormated());
        assertEquals("12-31", scv.getValidToFormated());
    }

    @Test
    public void findPublishableSensorConstantsForStation() {
        final String vakioNimi = "VVAPAAS1";
        final Integer vakioArvo = getRandomId(0, 20);

        final LamAnturiVakioVO vakio = tmsTestHelper.createAndSaveLamAnturiVakio(tmsStation.getLotjuId(), vakioNimi);
        tmsTestHelper.createAndSaveLamAnturiVakioArvo(vakio, vakioArvo);

        final TmsStationSensorConstantDtoV1 result =
            tmsDataWebServiceV1.findPublishableSensorConstantsForStation(tmsStation.getRoadStationNaturalId());
        AssertHelper.assertCollectionSize(1, result.sensorConstanValues);
        final TmsSensorConstantValueDto scv = result.sensorConstanValues.get(0);
        assertEquals(vakioNimi, scv.getName());
        assertEquals(vakioArvo, scv.getValue());
        assertEquals("01-01", scv.getValidFromFormated());
        assertEquals("12-31", scv.getValidToFormated());
    }
}