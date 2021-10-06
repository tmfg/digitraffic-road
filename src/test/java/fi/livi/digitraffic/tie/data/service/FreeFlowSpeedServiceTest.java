package fi.livi.digitraffic.tie.data.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dto.v1.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorConstantService;

public class FreeFlowSpeedServiceTest extends AbstractServiceTest {

    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Autowired
    private TmsStationSensorConstantService tmsStationSensorConstantService;

    @Autowired
    private DataStatusService dataStatusService;

    @BeforeEach
    public void initDb() {
        final TmsStation tms = TestUtils.generateDummyTmsStation();
        entityManager.persist(tms);
        entityManager.flush();
        TestUtils.commitAndEndTransactionAndStartNew(); // Native queries must see commits

        final LamAnturiVakioVO vakio1 = TestUtils.createLamAnturiVakio(tms.getLotjuId(), "VVAPAAS1");
        final LamAnturiVakioVO vakio2 = TestUtils.createLamAnturiVakio(tms.getLotjuId(), "VVAPAAS2");
        tmsStationSensorConstantService.updateSensorConstants(Arrays.asList(vakio1, vakio2));

        final LamAnturiVakioArvoVO v1Arvo = TestUtils.createLamAnturiVakioArvo(vakio1.getId(),101, 1231, 100);
        final LamAnturiVakioArvoVO v2Arvo = TestUtils.createLamAnturiVakioArvo(vakio2.getId(),101, 1231, 80);

        tmsStationSensorConstantService.updateSensorConstantValues(Arrays.asList(v1Arvo, v2Arvo));
        entityManager.flush();

        dataStatusService.updateDataUpdated(DataType.TMS_FREE_FLOW_SPEEDS_DATA);
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA_CHECK);
    }

    @AfterEach
    public void cleanDb() {
        TestUtils.truncateTmsData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew(); // persist changes
    }

    @Test
    public void testListAllLinkDataFromNonObsoleteStations() {
        final FreeFlowSpeedRootDataObjectDto object = freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(false);

        assertNotNull(object);
        assertNotNull(object.dataUpdatedTime);

        assertNotNull(object.getTmsFreeFlowSpeeds());
        assertTrue(object.getTmsFreeFlowSpeeds().size() > 0);
    }
}
