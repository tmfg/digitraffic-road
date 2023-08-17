package fi.livi.digitraffic.tie.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationSensorConstantDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationsSensorConstantsDataDtoV1;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDto;
import fi.livi.digitraffic.tie.service.tms.v1.TmsDataWebServiceV1;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuLAMMetatiedotServiceEndpointMock;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataClient;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorConstantUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationUpdater;

public class TmsStationSensorConstantsMetadataUpdateJobTest extends AbstractMetadataUpdateJobTest {

    private static final String MS1 = "MS1";
    private static final String MS2 = "MS2";
    private static final String VVAPAAS1 = "VVAPAAS1";
    private static final String VVAPAAS2 = "VVAPAAS2";
    private static final String TIEN_SUUNTA = "Tien_suunta";

    @Autowired
    private TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater;

    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    private TmsDataWebServiceV1 tmsDataWebServiceV1;

    @Autowired
    private LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceMock;

    @Autowired
    private LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @BeforeEach
    public void setFirstDestinationProviderForLotjuClients() {
        if (!isBeanRegistered(TmsDataWebServiceV1.class)) {
            final TmsDataWebServiceV1 tmsDataWebServiceV1 = beanFactory.createBean(TmsDataWebServiceV1.class);
            beanFactory.registerSingleton(tmsDataWebServiceV1.getClass().getCanonicalName(), tmsDataWebServiceV1);
            this.tmsDataWebServiceV1 = tmsDataWebServiceV1;
        }
        setLotjuClientFirstDestinationProviderAndSaveOriginalToMap(lotjuTmsStationMetadataClient);
    }

    @AfterEach
    public void restoreOriginalDestinationProviderForLotjuClients() {
        restoreLotjuClientDestinationProvider(lotjuTmsStationMetadataClient);
    }

    @Test
    public void testUpdateTmsStationsSensorConstants() {
        lotjuLAMMetatiedotServiceMock.initStateAndService();

        // Update TMS stations to initial state (ids: 1, 310 and 581 non obsolete stations and 2 obsolete)
        tmsStationUpdater.updateTmsStations();
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstants();
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantsValues();

        entityManager.flush();
        entityManager.clear();

        final TmsStationsSensorConstantsDataDtoV1 sensorConstantValuesBefore =
            tmsDataWebServiceV1.findPublishableSensorConstants(false);

        // Now change lotju metadata and update tms sensor constants
        lotjuLAMMetatiedotServiceMock.setStateAfterChange(true);
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstants();
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantsValues();

        entityManager.flush();
        entityManager.clear();

        final TmsStationsSensorConstantsDataDtoV1 sensorConstantValuesAfter =
            tmsDataWebServiceV1.findPublishableSensorConstants(false);

        // Station lotjuId: naturalId
        // 1: 23001
        // 310: 23826

        // Check that constants are created (MS1/MS2, VVAPAAS1/VVAPAAS2, TIEN_SUUNTA)
        // 1: VVAPAAS11 -> VVAPAAS1, MS1: null -> not null, MS2: not null to null
        assertNull(findConstantValue(MS1, 23001, sensorConstantValuesBefore));
        assertNotNull(findConstantValue(MS1, 23001, sensorConstantValuesAfter));

        assertNotNull(findConstantValue(MS2, 23001, sensorConstantValuesBefore));
        assertNull(findConstantValue(MS2, 23001, sensorConstantValuesAfter));

        assertNull(findConstantValue(VVAPAAS1, 23001, sensorConstantValuesBefore));
        assertNotNull(findConstantValue(VVAPAAS1, 23001, sensorConstantValuesAfter));

        // Not allowed name to publish
        assertNull(findConstantValue(VVAPAAS1 + "1", 23001, sensorConstantValuesBefore));
        assertNull(findConstantValue(VVAPAAS1 + "1", 23001, sensorConstantValuesAfter));

        assertNotNull(findConstantValue(VVAPAAS2, 23001, sensorConstantValuesBefore));
        assertNotNull(findConstantValue(VVAPAAS2, 23001, sensorConstantValuesAfter));

        assertNotNull(findConstantValue(TIEN_SUUNTA, 23001, sensorConstantValuesBefore));
        assertNotNull(findConstantValue(TIEN_SUUNTA, 23001, sensorConstantValuesAfter));

        // 310: has only values for VVAPAAS1/2,
        assertNull(findConstantValue(MS2, 23826, sensorConstantValuesBefore));
        assertNull(findConstantValue(MS2, 23826, sensorConstantValuesAfter));

        assertNotNull(findConstantValue(VVAPAAS1, 23826, sensorConstantValuesBefore));
        assertNotNull(findConstantValue(VVAPAAS1, 23826, sensorConstantValuesAfter));

        assertNotNull(findConstantValue(VVAPAAS2, 23826, sensorConstantValuesBefore));
        assertNotNull(findConstantValue(VVAPAAS2, 23826, sensorConstantValuesAfter));

        assertNull(findConstantValue(TIEN_SUUNTA, 23826, sensorConstantValuesBefore));
        assertNull(findConstantValue(TIEN_SUUNTA, 23826, sensorConstantValuesAfter));

        // Check constant values are updated
        final TmsSensorConstantValueDto vvapaas1WinterAfter = findConstantValue(VVAPAAS1, 23001, 101, sensorConstantValuesAfter);
        final TmsSensorConstantValueDto vvapaas1SummerAfter = findConstantValue(VVAPAAS1, 23001, 601, sensorConstantValuesAfter);
        // 95->100 ja 105->110
        assertEquals(100, (long) vvapaas1WinterAfter.getValue());
        assertEquals(110, (long) vvapaas1SummerAfter.getValue());

        final TmsSensorConstantValueDto vvapaas2WinterBefore = findConstantValue(VVAPAAS2, 23001, 101, sensorConstantValuesBefore);
        final TmsSensorConstantValueDto vvapaas2SummerBefore = findConstantValue(VVAPAAS2, 23001, 601, sensorConstantValuesBefore);
        final TmsSensorConstantValueDto vvapaas2WinterAfter = findConstantValue(VVAPAAS2, 23001, 101, sensorConstantValuesAfter);
        final TmsSensorConstantValueDto vvapaas2SummerAfter = findConstantValue(VVAPAAS2, 23001, 601, sensorConstantValuesAfter);
        // winter validity 1101 -> 1001 ja 331 -> 230, speed 95 -> 96
        // summer validity 401->301 ja 1031 -> 930, speed 105 -> 106
        assertEquals(1101, (long) vvapaas2WinterBefore.getValidFrom());
        assertEquals(1001, (long) vvapaas2WinterAfter.getValidFrom());
        assertEquals(331, (long) vvapaas2WinterBefore.getValidTo());
        assertEquals(230, (long) vvapaas2WinterAfter.getValidTo());
        assertEquals(95, (long) vvapaas2WinterBefore.getValue());
        assertEquals(96, (long) vvapaas2WinterAfter.getValue());
        assertEquals(401, (long) vvapaas2SummerBefore.getValidFrom());
        assertEquals(301, (long) vvapaas2SummerAfter.getValidFrom());
        assertEquals(1031, (long) vvapaas2SummerBefore.getValidTo());
        assertEquals(930, (long) vvapaas2SummerAfter.getValidTo());
        assertEquals(105, (long) vvapaas2SummerBefore.getValue());
        assertEquals(106, (long) vvapaas2SummerAfter.getValue());
    }

    private TmsSensorConstantValueDto findConstantValue(final String sensorConstantName, final long stationNaturalId,
                                                        final int validDate, final TmsStationsSensorConstantsDataDtoV1 sensorConstantValues) {
        final TmsStationSensorConstantDtoV1 stationConstants = findSensorConstantsOfStation(stationNaturalId, sensorConstantValues);
        return stationConstants.sensorConstanValues
            .stream()
            .filter(v -> v.getName().equals(sensorConstantName)
                     && isValidOn(validDate, v))
            .findFirst()
            .orElse(null);
    }

    private boolean isValidOn(final int validDate, final TmsSensorConstantValueDto v) {
        return (v.getValidFrom() <= validDate && v.getValidTo() >= validDate && v.getValidFrom() < v.getValidTo())
            || ( v.getValidFrom() >= v.getValidTo() && (validDate >= v.getValidFrom() || validDate <= v.getValidTo()) );
    }

    private TmsSensorConstantValueDto findConstantValue(final String sensorConstantName, final long stationNaturalId,
                                                        final TmsStationsSensorConstantsDataDtoV1 values) {
        final TmsStationSensorConstantDtoV1 constants = findSensorConstantsOfStation(stationNaturalId, values);
        if (constants != null) {
            return constants.sensorConstanValues
                .stream()
                .filter(sc -> sc.getName().equals(sensorConstantName))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    private TmsStationSensorConstantDtoV1 findSensorConstantsOfStation(final long stationNaturalId, final TmsStationsSensorConstantsDataDtoV1 values) {
        return values.stations.stream().filter(s -> s.id.equals(stationNaturalId)).findFirst().orElseThrow();
    }

}
