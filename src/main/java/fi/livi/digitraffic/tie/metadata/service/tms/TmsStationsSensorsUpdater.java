package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturiMapByTmsLotjuId = new HashMap<>();
        final Set<Long> tmsStationLotjuIdsWithError = new HashSet<>();

        log.info("Fetching LamLaskennallinenAnturis for " + tmsLotjuIds.size() + " LamAsemas");

        int counter = 0;
        for (final Long tmsStationLotjuId : tmsLotjuIds) {
            try {
                final List<LamLaskennallinenAnturiVO> anturis = lotjuTmsStationClient.getTiesaaLaskennallinenAnturis(tmsStationLotjuId);
                currentLamAnturiMapByTmsLotjuId.put(tmsStationLotjuId, anturis);
                counter += anturis.size();
            } catch (Exception e) {
                    tmsStationLotjuIdsWithError.add(tmsStationLotjuId);
                    log.error("Error while fetching LamLaskennallinenAnturi for LamAsema lotjuId: " + tmsStationLotjuId, e);
            }
        }

        log.info("Fetched " + counter + " LamLaskennallinenAnturis for " + (tmsLotjuIds.size()-tmsStationLotjuIdsWithError.size()) + " LamAsemas");
        if (!tmsStationLotjuIdsWithError.isEmpty()) {
            log.warn("Fetching LamLaskennallinenAnturis failed for " + tmsStationLotjuIdsWithError.size() + " LamAsemas");
        }

        // Update sensros of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfTmsStations(currentLamAnturiMapByTmsLotjuId,
                                           currentTmsStationMappedByByLotjuId,
                                           tmsStationLotjuIdsWithError);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update TMS Stations Sensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfTmsStations(
            final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamLaskennallinenAnturiMapByTmsStationLotjuId,
            final Map<Long, TmsStation> currentTmsStationMapByLotjuId,
            Set<Long> skipTmsStationsWithLotjuIds) {

        final Map<Long, RoadStationSensor> allSensors =
                roadStationSensorService.findAllRoadStationSensorsMappedByNaturalId(RoadStationType.TMS_STATION);

        final Iterator<Map.Entry<Long, List<LamLaskennallinenAnturiVO>>> entryIter =
                currentLamLaskennallinenAnturiMapByTmsStationLotjuId.entrySet().iterator();

        int countAdd = 0;
        int countRemove = 0;
        while (entryIter.hasNext()) {
            final Map.Entry<Long, List<LamLaskennallinenAnturiVO>> entry = entryIter.next();
            final Long tmsStationLotjuId = entry.getKey();
            final List<LamLaskennallinenAnturiVO> lamAnturis = entry.getValue();
            entryIter.remove();

            final TmsStation tmsStation = currentTmsStationMapByLotjuId.remove(tmsStationLotjuId);

            if (skipTmsStationsWithLotjuIds.contains(tmsStationLotjuId)) {
                log.warn("Skip TmsStation with lotjuId " + tmsStationLotjuId);
            } else if (tmsStation == null) {
                log.error("No WeatherStation found for lotjuId " + tmsStationLotjuId);
            } else {

                final RoadStation rs = tmsStation.getRoadStation();

                final List<RoadStationSensor> sensors = rs.getRoadStationSensors();
                final Map<Long, RoadStationSensor> naturalIdToCurrentSensorMap = new HashMap<>();
                for (final RoadStationSensor sensor : sensors) {
                    naturalIdToCurrentSensorMap.put(sensor.getNaturalId(), sensor);
                }

                for (final LamLaskennallinenAnturiVO anturi : lamAnturis) {
                    if ( addSensorIfMissing(rs, naturalIdToCurrentSensorMap, anturi, allSensors) ) {
                        countAdd++;
                    }
                }

                // Remove non existing sensors that are left in map
                for (final RoadStationSensor remove : naturalIdToCurrentSensorMap.values()) {
                    rs.getRoadStationSensors().remove(remove);
                    countRemove++;
                    log.info("Removed " + remove + " from " + rs);
                }
            }
        }

        // remove non exiting sensors from not found tms stations
        for (TmsStation tmsStation : currentTmsStationMapByLotjuId.values()) {
            if (!skipTmsStationsWithLotjuIds.contains(tmsStation.getLotjuId())) {
                tmsStation.getRoadStation().getRoadStationSensors().clear();
            }
        }


        int notFound = 0;
        for (final List<LamLaskennallinenAnturiVO> values : currentLamLaskennallinenAnturiMapByTmsStationLotjuId.values()) {
            notFound =+ values.size();
        }
        log.info("RoadStation not found for " + notFound + " TiesaaLaskennallinenAnturis");
        log.info("Sensor removed from road stations " + countRemove);
        log.info("Sensor added to road stations " + countAdd);

        return countAdd > 0 || countRemove > 0;
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
