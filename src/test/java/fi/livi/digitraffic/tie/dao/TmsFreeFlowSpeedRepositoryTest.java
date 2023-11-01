package fi.livi.digitraffic.tie.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.tms.TmsFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsFreeFlowSpeedDto;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.tms.TmsStationSensorConstantService;

public class TmsFreeFlowSpeedRepositoryTest extends AbstractServiceTest {

    @Autowired
    private TmsFreeFlowSpeedRepository freeFlowSpeedService;

    @Autowired
    private TmsStationSensorConstantService tmsStationSensorConstantService;

    private TmsStation tms;

    private static final int VVAPAAS1 = 100;
    private static final int VVAPAAS2 = 80;

    @BeforeEach
    public void initDb() {
        tms = TestUtils.generateDummyTmsStation();
        entityManager.persist(tms);
        entityManager.flush();
        TestUtils.commitAndEndTransactionAndStartNew(); // Native queries must see commits

        final LamAnturiVakioVO vapaaNopeusVakio1 = TestUtils.createLamAnturiVakio(tms.getLotjuId(), "VVAPAAS1");
        final LamAnturiVakioVO vapaaNopeusVakio2 = TestUtils.createLamAnturiVakio(tms.getLotjuId(), "VVAPAAS2");
        tmsStationSensorConstantService.updateSensorConstants(Arrays.asList(vapaaNopeusVakio1, vapaaNopeusVakio2));

        final LamAnturiVakioArvoVO v1Arvo = TestUtils.createLamAnturiVakioArvo(vapaaNopeusVakio1.getId(),101, 1231, VVAPAAS1);
        final LamAnturiVakioArvoVO v2Arvo = TestUtils.createLamAnturiVakioArvo(vapaaNopeusVakio2.getId(),101, 1231, VVAPAAS2);

        tmsStationSensorConstantService.updateSensorConstantValues(Arrays.asList(v1Arvo, v2Arvo));
        entityManager.flush();
    }

    @AfterEach
    public void cleanDb() {
        TestUtils.truncateTmsData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew(); // persist changes
    }

    @Test
    public void getTmsFreeFlowSpeedsByRoadStationNaturalId() {
        final TmsFreeFlowSpeedDto ffs =
            freeFlowSpeedService.getTmsFreeFlowSpeedsByRoadStationNaturalId(tms.getRoadStationNaturalId());
        assertEquals(VVAPAAS1, ffs.getFreeFlowSpeed1());
        assertEquals(VVAPAAS2, ffs.getFreeFlowSpeed2());
    }
}
