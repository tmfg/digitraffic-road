package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Table;

import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationMetadataService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioArvoVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioVO;

@Service
public class TmsStationSensorConstantUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantUpdater.class);

    private final TmsStationSensorConstantService tmsStationSensorConstantService;
    private final TmsStationService tmsStationService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationSensorConstantUpdater(final TmsStationSensorConstantService tmsStationSensorConstantService,
                                           final TmsStationService tmsStationService,
                                           final DataStatusService dataStatusService,
                                           final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        this.tmsStationSensorConstantService = tmsStationSensorConstantService;
        this.tmsStationService = tmsStationService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    /**
     * Updates all available sensorConstants of tms road stations
     */
    public boolean updateTmsStationsSensorConstants() {
        log.info("Update TMS Stations SensorConstants start");

        // Get current TmsStations
        final Map<Long, TmsStation> currentTmsStationMappedByByLotjuId =
            tmsStationService.findAllTmsStationsMappedByByLotjuId();

        final Set<Long> tmsLotjuIds = currentTmsStationMappedByByLotjuId.keySet();

        log.info("Fetching LamAnturiVakios for tmsCount={} LamAsemas", tmsLotjuIds.size());

        List<LamAnturiVakioVO> allLamAnturiVakios = lotjuTmsStationMetadataService.getAllLamAnturiVakios(tmsLotjuIds);

        final boolean updateStaticDataStatus = tmsStationSensorConstantService.updateSensorConstants(allLamAnturiVakios);


        log.info("Update TMS Stations SensorConstants end");
        return updateStaticDataStatus;
    }

    /**
     * Updates all available sensorConstants of tms road stations
     */
    public boolean updateTmsStationsSensorConstantsValues() {
        log.info("Update TMS Stations SensorConstantValues start");

        final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos = lotjuTmsStationMetadataService.getAllLamAnturiVakioArvos();

        log.info("allLamAnturiVakioArvos.size() {}", allLamAnturiVakioArvos.size());

        int count = tmsStationSensorConstantService.updateSensorConstantValues(allLamAnturiVakioArvos);
        log.info("SensorConstantValues count {}", count);

        int countFreeFlowSpeeds = tmsStationSensorConstantService.updateFreeFlowSpeedsOfTmsStations();
        log.info("Update FreeFlowSpeeds for {} TmsStations", countFreeFlowSpeeds);

        log.info("Update TMS Stations SensorConstantValues end");
        return true;
    }
}
