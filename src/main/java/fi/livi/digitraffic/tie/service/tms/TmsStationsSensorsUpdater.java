package fi.livi.digitraffic.tie.service.tms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.lotju.LotjuTmsStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class TmsStationsSensorsUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationsSensorsUpdater.class);

    private final RoadStationSensorService roadStationSensorService;
    private final TmsStationService tmsStationService;
    private final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper;
    private final DataStatusService dataStatusService;

    @Autowired
    public TmsStationsSensorsUpdater(final RoadStationSensorService roadStationSensorService,
                                     final TmsStationService tmsStationService,
                                     final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper,
                                     final DataStatusService dataStatusService) {
        this.roadStationSensorService = roadStationSensorService;
        this.tmsStationService = tmsStationService;
        this.lotjuTmsStationMetadataClientWrapper = lotjuTmsStationMetadataClientWrapper;
        this.dataStatusService = dataStatusService;
    }

    /**
     * Updates all available sensors of tms road stations
     */
    public boolean updateTmsStationsSensors() {
        log.info("Update TMS Stations Sensors start");

        // Update sensors of road stations
        // Get current TmsStations
        final Map<Long, TmsStation> currentTmsStationMappedByByLotjuId =
                tmsStationService.findAllTmsStationsMappedByByLotjuId();

        final Set<Long> tmsLotjuIds = currentTmsStationMappedByByLotjuId.keySet();

        log.info("Fetching LamLaskennallinenAnturis for tmsCount={} LamAsemas", tmsLotjuIds.size());

        final Map<Long, List<LamLaskennallinenAnturiVO>> anturisMappedByAsemaLotjuId =
                lotjuTmsStationMetadataClientWrapper.getLamLaskennallinenAnturisMappedByAsemaLotjuId(tmsLotjuIds);

        final List<Pair<TmsStation,  List<LamLaskennallinenAnturiVO>>> stationAnturisPairs = new ArrayList<>();
        currentTmsStationMappedByByLotjuId.values().forEach(tmsStation -> {
            final List<LamLaskennallinenAnturiVO> anturis = anturisMappedByAsemaLotjuId.remove(tmsStation.getLotjuId());
            if (anturis != null) {
                stationAnturisPairs.add(Pair.of(tmsStation, anturis));
            } else {
                log.info("No anturis for {}", tmsStation);
                stationAnturisPairs.add(Pair.of(tmsStation, Collections.emptyList()));
            }
        });

        // Update sensors of road stations
        final boolean updateStaticDataStatus = updateSensorsOfTmsStations(stationAnturisPairs);
        if (updateStaticDataStatus) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
        }
        log.info("Update TMS Stations Sensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfTmsStations(final List<Pair<TmsStation,  List<LamLaskennallinenAnturiVO>>> stationAnturisPairs) {

        int countAdded = 0;
        int countRemoved = 0;

        for (final Pair<TmsStation, List<LamLaskennallinenAnturiVO>> pair : stationAnturisPairs) {
            final TmsStation tms = pair.getKey();
            final List<LamLaskennallinenAnturiVO> anturis = pair.getRight();
            try {
                final List<Long> sensorslotjuIds = anturis.stream().map(LamLaskennallinenAnturiVO::getId).collect(Collectors.toList());
                final Pair<Integer, Integer> deletedInserted =
                    roadStationSensorService.updateSensorsOfRoadStation(tms.getRoadStationId(),
                                                                        RoadStationType.TMS_STATION,
                                                                        sensorslotjuIds);
                countRemoved += deletedInserted.getLeft();
                countAdded += deletedInserted.getRight();
            } catch (final Exception e) {
                log.info("Anturis count={}", anturis);
                throw e;
            }
        }

        log.info("Sensor removed from road stations countRemoved={}", countRemoved);
        log.info("Sensor added to road stations countAdded={}", countAdded);

        return countRemoved > 0 || countAdded > 0;
    }
}
