package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.AbstractWeatherStationAttributeUpdater;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;

@Service
public class TmsStationsSensorsUpdater extends AbstractWeatherStationAttributeUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationsSensorsUpdater.class);

    private RoadStationSensorService roadStationSensorService;
    private final TmsStationService tmsStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuTmsStationClient lotjuTmsStationClient;

    @Autowired
    public TmsStationsSensorsUpdater(final RoadStationService roadStationService,
                                     final RoadStationSensorService roadStationSensorService,
                                     final TmsStationService tmsStationService,
                                     final StaticDataStatusService staticDataStatusService,
                                     final LotjuTmsStationClient lotjuTmsStationClient) {
        super(roadStationService);
        this.roadStationSensorService = roadStationSensorService;
        this.tmsStationService = tmsStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuTmsStationClient = lotjuTmsStationClient;
    }

    /**
     * Updates all available sensors of weather road stations
     */
    @Transactional
    public boolean updateTmsStationsSensors() {
        log.info("Update TMS Stations Sensors start");

        if (lotjuTmsStationClient == null) {
            log.warn("Not updating TMS Stations Sensors metadata because lotjuTmsStationClient not defined");
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
                lotjuTmsStationClient.getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(tmsLotjuIds);

        log.info("Fetched {} LamLaskennallinenAnturis for {} LamAsemas", counter, tmsLotjuIds.size());


        final List<Pair<TmsStation,  List<LamLaskennallinenAnturiVO>>> stationAnturisPairs = new ArrayList<>();
        currentTmsStationMappedByByLotjuId.values().stream().forEach(tmsStation -> {
            final List<LamLaskennallinenAnturiVO> anturis = anturisMappedByAsemaLotjuId.remove(tmsStation.getLotjuId());
            stationAnturisPairs.add(Pair.of(tmsStation, anturis));
        });

        // Update sensors of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfTmsStations(stationAnturisPairs);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update TMS Stations Sensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfTmsStations(final List<Pair<TmsStation,  List<LamLaskennallinenAnturiVO>>> stationAnturisPairs) {

        final Map<Long, RoadStationSensor> allSensorsMappedByLotjuId =
                roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.TMS_STATION);

        final AtomicInteger countAdded = new AtomicInteger();
        final AtomicInteger countRemoved = new AtomicInteger();

        stationAnturisPairs.stream().forEach(pair -> {
            TmsStation station = pair.getKey();
            List<RoadStationSensor> rsSensors = station.getRoadStation().getRoadStationSensors();
            List<LamLaskennallinenAnturiVO> anturis = pair.getValue();

            if (anturis != null) {
                anturis.stream().forEach(anturi -> {
                    RoadStationSensor sensor = allSensorsMappedByLotjuId.get(anturi.getId());
                    Optional<RoadStationSensor> existingSensor =
                            rsSensors.stream().filter(s -> anturi.getId().equals(s.getLotjuId())).findFirst();
                    if (sensor == null) {
                        log.error("No Weather RoadStationSensor found with lotjuId {}", anturi.getId());
                    } else if (!existingSensor.isPresent()) {
                        rsSensors.add(sensor);
                        countAdded.addAndGet(1);
                        log.info("Add sensor {} for {}", sensor, station);
                    }
                });
            }

            final List<RoadStationSensor> toRemove = rsSensors.stream().filter(s -> s.getLotjuId() == null || anturis == null ||
                    !anturis.stream().filter(a -> a.getId().equals(s.getLotjuId())).findFirst().isPresent()).collect(Collectors.toList());
            countRemoved.addAndGet(toRemove.size());
            rsSensors.removeAll(toRemove);
        });

        log.info("Sensor removed from road stations {}", countRemoved);
        log.info("Sensor added to road stations {}", countAdded);

        return countRemoved.get() > 0 || countAdded.get() > 0;
    }

    private static boolean addSensorIfMissing(RoadStation rs,
                                              Map<Long, RoadStationSensor> naturalIdToCurrentSensorMap,
                                              LamLaskennallinenAnturiVO anturi,
                                              Map<Long, RoadStationSensor> allSensors) {
        final Long sensorNaturalId = Long.valueOf(anturi.getVanhaId());
        final RoadStationSensor sensor = naturalIdToCurrentSensorMap.remove(sensorNaturalId);
        // road station doesn't have mapping for sensor -> add it
        if (sensor == null) {
            final RoadStationSensor add = allSensors.get(sensorNaturalId);
            if (add == null) {
                log.error("No RoadStationSensor found with naturalId " + sensorNaturalId);
            } else {
                rs.getRoadStationSensors().add(add);
                log.info("Add sensor " + add + " for " + rs);
                return true;
            }
        }
        return false;
    }

    private void updateRoasWeatherSensorStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER_SENSOR, updateStaticDataStatus);
    }
}
