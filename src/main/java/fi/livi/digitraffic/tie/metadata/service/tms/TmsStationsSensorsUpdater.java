package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;

@Service
public class TmsStationsSensorsUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationsSensorsUpdater.class);

    private RoadStationSensorService roadStationSensorService;
    private final TmsStationService tmsStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationsSensorsUpdater(final RoadStationSensorService roadStationSensorService,
                                     final TmsStationService tmsStationService,
                                     final StaticDataStatusService staticDataStatusService,
                                     final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        this.roadStationSensorService = roadStationSensorService;
        this.tmsStationService = tmsStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    /**
     * Updates all available sensors of weather road stations
     */
    public boolean updateTmsStationsSensors() {
        log.info("Update TMS Stations Sensors start");

        if (!lotjuTmsStationMetadataService.isEnabled()) {
            log.warn("Not updating TMS Stations Sensors metadata because LotjuTmsStationMetadataService not enabled");
            return false;
        }

        // Update sensors of road stations
        // Get current TmsStations
        final Map<Long, TmsStation> currentTmsStationMappedByByLotjuId =
                tmsStationService.findAllTmsStationsMappedByByLotjuId();

        final Set<Long> tmsLotjuIds = currentTmsStationMappedByByLotjuId.keySet();

        log.info("Fetching LamLaskennallinenAnturis for " + tmsLotjuIds.size() + " LamAsemas");

        final AtomicInteger counter = new AtomicInteger();
        Map<Long, List<LamLaskennallinenAnturiVO>> anturisMappedByAsemaLotjuId =
                lotjuTmsStationMetadataService.getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(tmsLotjuIds);

        log.info("Fetched {} LamLaskennallinenAnturis for {} LamAsemas", counter, tmsLotjuIds.size());


        final List<Pair<TmsStation,  List<LamLaskennallinenAnturiVO>>> stationAnturisPairs = new ArrayList<>();
        currentTmsStationMappedByByLotjuId.values().stream().forEach(tmsStation -> {
            final List<LamLaskennallinenAnturiVO> anturis = anturisMappedByAsemaLotjuId.remove(tmsStation.getLotjuId());
            if (anturis != null) {
                stationAnturisPairs.add(Pair.of(tmsStation, anturis));
            } else {
                log.error("No anturis for " + tmsStation);
                stationAnturisPairs.add(Pair.of(tmsStation, Collections.emptyList()));
            }
        });

        // Update sensors of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfTmsStations(stationAnturisPairs);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update TMS Stations Sensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfTmsStations(final List<Pair<TmsStation,  List<LamLaskennallinenAnturiVO>>> stationAnturisPairs) {

        final AtomicInteger countAdded = new AtomicInteger();
        final AtomicInteger countRemoved = new AtomicInteger();

        stationAnturisPairs.stream().forEach(pair -> {
            final TmsStation tms = pair.getKey();
            final List<LamLaskennallinenAnturiVO> anturis = pair.getRight();
            try {
                final List<Long> sensorslotjuIds = anturis.stream().map(LamLaskennallinenAnturiVO::getId).collect(Collectors.toList());
                Pair<Integer, Integer> deletedInserted = roadStationSensorService.updateSensorsOfWeatherStations(tms.getRoadStationId(),
                    RoadStationType.TMS_STATION,
                    sensorslotjuIds);
                countRemoved.addAndGet(deletedInserted.getLeft());
                countAdded.addAndGet(deletedInserted.getRight());
            } catch (Exception e) {
                e.printStackTrace();
                log.info("Anturis {}", anturis);
                throw e;
            }
        });

        log.info("Sensor removed from road stations {}", countRemoved);
        log.info("Sensor added to road stations {}", countAdded);

        return countRemoved.get() > 0 || countAdded.get() > 0;
    }

    private void updateRoasWeatherSensorStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER_SENSOR, updateStaticDataStatus);
    }
}
