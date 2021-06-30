package fi.livi.digitraffic.tie.service.v1.tms;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class TmsStationSensorConstantUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantUpdater.class);

    private final TmsStationSensorConstantService tmsStationSensorConstantService;
    private final TmsStationService tmsStationService;
    private final DataStatusService dataStatusService;
    private final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper;

    @Autowired
    public TmsStationSensorConstantUpdater(final TmsStationSensorConstantService tmsStationSensorConstantService,
                                           final TmsStationService tmsStationService,
                                           final DataStatusService dataStatusService,
                                           final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper) {
        this.tmsStationSensorConstantService = tmsStationSensorConstantService;
        this.tmsStationService = tmsStationService;
        this.dataStatusService = dataStatusService;
        this.lotjuTmsStationMetadataClientWrapper = lotjuTmsStationMetadataClientWrapper;
    }

    /**
     * Updates all available sensorConstants of tms road stations
     */
    public boolean updateTmsStationsSensorConstants() {
        final StopWatch start = StopWatch.createStarted();

        // Get current TmsStations
        final List<Long> tmsLotjuIds =
            tmsStationService.findAllTmsStationsLotjuIds();

        final List<LamAnturiVakioVO> allLamAnturiVakios = lotjuTmsStationMetadataClientWrapper.getAllLamAnturiVakios(tmsLotjuIds);

        final boolean updated = tmsStationSensorConstantService.updateSensorConstants(allLamAnturiVakios);

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA_CHECK);

        log.info("method=updateTmsStationsSensorConstants tms count={} tookMs={}", tmsLotjuIds.size(), start.getTime());
        return updated;
    }

    /**
     * Updates all available sensorConstants of tms road stations
     */
    public boolean updateTmsStationsSensorConstantsValues() {
        final StopWatch start = StopWatch.createStarted();

        final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos = lotjuTmsStationMetadataClientWrapper.getAllLamAnturiVakioArvos();

        boolean updated = tmsStationSensorConstantService.updateSensorConstantValues(allLamAnturiVakioArvos);

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.TMS_FREE_FLOW_SPEEDS_DATA);
            dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA_CHECK);

        log.info("method=updateTmsStationsSensorConstantsValues tookMs={}", start.getTime());
        return updated;
    }
}
