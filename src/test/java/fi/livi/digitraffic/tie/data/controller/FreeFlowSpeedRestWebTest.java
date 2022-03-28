package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FREE_FLOW_SPEEDS_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorConstantService;

public class FreeFlowSpeedRestWebTest extends AbstractRestWebTest {

    @Autowired
    private TmsStationSensorConstantService tmsStationSensorConstantService;

    @Autowired
    private DataStatusService dataStatusService;

    private Long tmsId;

    @BeforeEach
    public void initDb() {
        final TmsStation tms = TestUtils.generateDummyTmsStation();
        entityManager.persist(tms);
        entityManager.flush();
        TestUtils.commitAndEndTransactionAndStartNew(); // Native queries must see commits

        final LamAnturiVakioVO vakio1 = TestUtils.createLamAnturiVakio(tms.getLotjuId(), "VVAPAAS1");
        final LamAnturiVakioVO vakio2 = TestUtils.createLamAnturiVakio(tms.getLotjuId(), "VVAPAAS2");
        tmsStationSensorConstantService.updateSensorConstants(Arrays.asList(vakio1, vakio2));

        final LamAnturiVakioArvoVO v1Arvo1 = TestUtils.createLamAnturiVakioArvo(vakio1.getId(),101, 630, 100);
        final LamAnturiVakioArvoVO v1Arvo2 = TestUtils.createLamAnturiVakioArvo(vakio1.getId(),701, 1231, 100);
        final LamAnturiVakioArvoVO v2Arvo1 = TestUtils.createLamAnturiVakioArvo(vakio2.getId(),101, 630, 80);
        final LamAnturiVakioArvoVO v2Arvo2 = TestUtils.createLamAnturiVakioArvo(vakio2.getId(),701, 1231, 80);
        tmsStationSensorConstantService.updateSensorConstantValues(Arrays.asList(v1Arvo1, v1Arvo2, v2Arvo1, v2Arvo2));
        entityManager.flush();
        tmsId = tms.getNaturalId();

        dataStatusService.updateDataUpdated(DataType.TMS_FREE_FLOW_SPEEDS_DATA);
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA_CHECK);
    }

    @AfterEach
    public void cleanDb() {
        TestUtils.truncateTmsData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew(); // Commit changes
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    @Test
    public void testFreeFlowSpeedDataRestApi() throws Exception {

        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + FREE_FLOW_SPEEDS_PATH))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.tmsFreeFlowSpeeds", Matchers.notNullValue()))
            .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].id", Matchers.notNullValue()))
            .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].tmsNumber", Matchers.notNullValue()))
            .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].tmsNumber", Matchers.notNullValue()))
            .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].freeFlowSpeed1", Matchers.equalTo(100.0)))
            .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].freeFlowSpeed2", Matchers.equalTo(80.0)))
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER);
    }

    @Test
    public void testFreeFlowSpeedDataRestApiByTmsId() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + FREE_FLOW_SPEEDS_PATH + "/tms/" + tmsId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].freeFlowSpeed1", Matchers.equalTo(100.0)))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].freeFlowSpeed2", Matchers.equalTo(80.0)))
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER);
    }
}
