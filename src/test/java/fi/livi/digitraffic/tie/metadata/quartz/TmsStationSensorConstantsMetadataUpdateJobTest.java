package fi.livi.digitraffic.tie.metadata.quartz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstant;
import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstantValue;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuLAMMetatiedotServiceEndpointMock;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationSensorConstantUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationUpdater;

public class TmsStationSensorConstantsMetadataUpdateJobTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantsMetadataUpdateJobTest.class);

    private static final String MS1 = "MS1";
    private static final String MS2 = "MS2";
    private static final String VVAPAAS1 = "VVAPAAS1";
    private static final String VVAPAAS2 = "VVAPAAS2";
    private static final String TIEN_SUUNTA = "Tien_suunta";

    @Autowired
    private TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater;

    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    @Autowired
    TmsStationSensorConstantService tmsStationSensorConstantService;

    @Autowired
    private LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceMock;

    @Test
    public void testUpdateTmsStationsSensorConstants() {
        lotjuLAMMetatiedotServiceMock.initStateAndService();

        // Update TMS stations to initial state (ids: 1, 310 and 581 non obsolete stations and 2 obsolete)
        tmsStationUpdater.updateTmsStations();
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstants();
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantsValues();

        entityManager.flush();
        entityManager.clear();

        final List<TmsSensorConstant> sensorConstantsBefore =
            tmsStationSensorConstantService.findAllPublishableTmsStationsSensorConstants();
        final List<TmsSensorConstantValue> sensorConstantValuesBefore =
            tmsStationSensorConstantService.findAllPublishableTmsStationsSensorConstantValues();
        // lazy fetch
        sensorConstantsBefore.stream().forEach(sc -> sc.getRoadStation().getLotjuId());
        sensorConstantValuesBefore.stream().forEach(v -> v.getSensorConstant().getRoadStation());

        entityManager.flush();
        entityManager.clear();

        // Now change lotju metadata and update tms sensor constants
        lotjuLAMMetatiedotServiceMock.setStateAfterChange(true);
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstants();
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantsValues();

        entityManager.flush();
        entityManager.clear();

        final List<TmsSensorConstant> sensorConstantsAfter =
            tmsStationSensorConstantService.findAllPublishableTmsStationsSensorConstants();
        final List<TmsSensorConstantValue> sensorConstantValuesAfter =
            tmsStationSensorConstantService.findAllPublishableTmsStationsSensorConstantValues();

        // Check that constants are created (MS1/MS2, VVAPAAS1/VVAPAAS2, TIEN_SUUNTA)
        // 1: VVAPAAS11 -> VVAPAAS1, MS1: null -> not null, MS2: not null to null
        assertNull(findConstant(MS1, 1, sensorConstantsBefore));
        assertNotNull(findConstant(MS1, 1, sensorConstantsAfter));

        assertNotNull(findConstant(MS2, 1, sensorConstantsBefore));
        assertNull(findConstant(MS2, 1, sensorConstantsAfter));

        assertNull(findConstant(VVAPAAS1, 1, sensorConstantsBefore));
        assertNotNull(findConstant(VVAPAAS1, 1, sensorConstantsAfter));

        assertNotNull(findConstant(VVAPAAS1 + "1", 1, sensorConstantsBefore));
        assertNull(findConstant(VVAPAAS1 + "1", 1, sensorConstantsAfter));

        assertNotNull(findConstant(VVAPAAS2, 1, sensorConstantsBefore));
        assertNotNull(findConstant(VVAPAAS2, 1, sensorConstantsAfter));

        assertNotNull(findConstant(TIEN_SUUNTA, 1, sensorConstantsBefore));
        assertNotNull(findConstant(TIEN_SUUNTA, 1, sensorConstantsAfter));
        // 310:
        assertNotNull(findConstant(MS2, 310, sensorConstantsBefore));
        assertNotNull(findConstant(MS2, 310, sensorConstantsAfter));

        assertNotNull(findConstant(VVAPAAS1, 310, sensorConstantsBefore));
        assertNotNull(findConstant(VVAPAAS1, 310, sensorConstantsAfter));

        assertNotNull(findConstant(VVAPAAS2, 310, sensorConstantsBefore));
        assertNotNull(findConstant(VVAPAAS2, 310, sensorConstantsAfter));

        assertNotNull(findConstant(TIEN_SUUNTA, 310, sensorConstantsBefore));
        assertNotNull(findConstant(TIEN_SUUNTA, 310, sensorConstantsAfter));

        // Check constant values are updated
        TmsSensorConstantValue vvapaas1WinterBefore = findConstantValue(VVAPAAS1 + "1", 1, 101, sensorConstantValuesBefore);
        TmsSensorConstantValue vvapaas1SummerBefore = findConstantValue(VVAPAAS1 + "1", 1, 601, sensorConstantValuesBefore);
        TmsSensorConstantValue vvapaas1WinterAfter = findConstantValue(VVAPAAS1, 1, 101, sensorConstantValuesAfter);
        TmsSensorConstantValue vvapaas1SummerAfter = findConstantValue(VVAPAAS1, 1, 601, sensorConstantValuesAfter);
        // 95->100 ja 105->110
        assertEquals(95, (long) vvapaas1WinterBefore.getValue());
        assertEquals(100, (long) vvapaas1WinterAfter.getValue());
        assertEquals(105, (long) vvapaas1SummerBefore.getValue());
        assertEquals(110, (long) vvapaas1SummerAfter.getValue());

        TmsSensorConstantValue vvapaas2WinterBefore = findConstantValue(VVAPAAS2, 1, 101, sensorConstantValuesBefore);
        TmsSensorConstantValue vvapaas2SummerBefore = findConstantValue(VVAPAAS2, 1, 601, sensorConstantValuesBefore);
        TmsSensorConstantValue vvapaas2WinterAfter = findConstantValue(VVAPAAS2, 1, 101, sensorConstantValuesAfter);
        TmsSensorConstantValue vvapaas2SummerAfter = findConstantValue(VVAPAAS2, 1, 601, sensorConstantValuesAfter);
        // winter validity 1101 -> 1001 ja 331 -> 230
        // summer validity 401->301 ja 1031 -> 930
        assertEquals(1101, (long) vvapaas2WinterBefore.getValidFrom());
        assertEquals(1001, (long) vvapaas2WinterAfter.getValidFrom());
        assertEquals(331, (long) vvapaas2WinterBefore.getValidTo());
        assertEquals(230, (long) vvapaas2WinterAfter.getValidTo());
        assertEquals(401, (long) vvapaas2SummerBefore.getValidFrom());
        assertEquals(301, (long) vvapaas2SummerAfter.getValidFrom());
        assertEquals(1031, (long) vvapaas2SummerBefore.getValidTo());
        assertEquals(930, (long) vvapaas2SummerAfter.getValidTo());
    }

    private TmsSensorConstantValue findConstantValue(final String sensorConstantName, final long stationLotjuId, final int validDate,
                                                     final List<TmsSensorConstantValue> sensorConstantValues) {
        return sensorConstantValues
            .stream()
            .filter(v -> v.getSensorConstant().getRoadStation().getLotjuId().equals(stationLotjuId)
                     &&  v.getSensorConstant().getName().equals(sensorConstantName)
                     && isValidOn(validDate, v))
            .findFirst()
            .orElse(null);
    }

    private boolean isValidOn(final int validDate, final TmsSensorConstantValue v) {
        return (v.getValidFrom() <= validDate && v.getValidTo() >= validDate && v.getValidFrom() < v.getValidTo())
            || ( v.getValidFrom() >= v.getValidTo() && (validDate >= v.getValidFrom() || validDate <= v.getValidTo()) );
    }

    private TmsSensorConstant findConstant(final String sensorConstantName, final long stationLotjuId, final List<TmsSensorConstant> sensorConstants) {
        return sensorConstants
            .stream()
            .filter(sc -> sc.getRoadStation().getLotjuId().equals(stationLotjuId) && sc.getName().equals(sensorConstantName))
            .findFirst()
            .orElse(null);
    }
}
