package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_DATA_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.dao.v1.SensorValueRepository;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.SensorValue;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

public class TmsStationDataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private SensorValueRepository sensorValueRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    private long tmsNaturalId;

    @BeforeEach
    public void initDb() {
        final TmsStation tms = TestUtils.generateDummyTmsStation();
        entityManager.persist(tms);
        tmsNaturalId = tms.getNaturalId();

        final List<RoadStationSensor> sensors =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);

        final SensorValue sv1 = new SensorValue(tms.getRoadStation(), sensors.get(0), 10.0, ZonedDateTime.now());
        final SensorValue sv2 = new SensorValue(tms.getRoadStation(), sensors.get(1), 10.0, ZonedDateTime.now());
        sensorValueRepository.save(sv1);
        sensorValueRepository.save(sv2);

        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.TMS_STATION));
    }

    @Test
    public void testTmsDataRestApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + TMS_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
        ;
    }

    @Test
    public void testTmsDataRestApiById() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + TMS_DATA_PATH + "/" + tmsNaturalId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].measuredTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
        ;
    }
}
