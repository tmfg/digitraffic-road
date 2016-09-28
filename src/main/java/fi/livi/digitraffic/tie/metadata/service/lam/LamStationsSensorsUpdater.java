package fi.livi.digitraffic.tie.metadata.service.lam;

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

import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuLamStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.AbstractWeatherStationUpdater;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;

@Service
public class LamStationsSensorsUpdater extends AbstractWeatherStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(LamStationsSensorsUpdater.class);

    private RoadStationSensorService roadStationSensorService;
    private final LamStationService lamStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuLamStationClient lotjuLamStationClient;

    @Autowired
    public LamStationsSensorsUpdater(final RoadStationService roadStationService,
                                     final RoadStationSensorService roadStationSensorService,
                                     final LamStationService lamStationService,
                                     final StaticDataStatusService staticDataStatusService,
                                     final LotjuLamStationClient lotjuLamStationClient) {
        super(roadStationService);
        this.roadStationSensorService = roadStationSensorService;
        this.lamStationService = lamStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuLamStationClient = lotjuLamStationClient;
    }

    /**
     * Updates all available sensors of weather road stations
     */
    @Transactional
    public boolean updateLamStationsSensors() {
        log.info("Update LamStations Sensors start");

        if (lotjuLamStationClient == null) {
            log.warn("Not updating LamStations Sensors metadatas because no lotjuLamStationClient defined");
            return false;
        }

        // Update sensors of road stations
        // Get current LamStations
        final Map<Long, LamStation> currentLamStationMapByLotjuId =
                lamStationService.findAllLamStationsByMappedByLotjuId();

        final Set<Long> lamsLotjuIds = currentLamStationMapByLotjuId.keySet();

        final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturiMapByLamLotjuId = new HashMap<>();
        final Set<Long> lamAsemaLotjuIdsWithError = new HashSet<>();

        log.info("Fetching LamLaskennallinenAnturis for " + lamsLotjuIds.size() + " LamAsemas");

        int counter = 0;
        for (final Long lamAsemaLotjuId : lamsLotjuIds) {
            try {
                final List<LamLaskennallinenAnturiVO> anturis = lotjuLamStationClient.getTiesaaLaskennallinenAnturis(lamAsemaLotjuId);
                currentLamAnturiMapByLamLotjuId.put(lamAsemaLotjuId, anturis);
                counter += anturis.size();
            } catch (Exception e) {
                    lamAsemaLotjuIdsWithError.add(lamAsemaLotjuId);
                    log.error("Error while fetching lam's sensors for station lotjuId: " + lamAsemaLotjuId, e);
            }
        }

        log.info("Fetched " + counter + " LamLaskennallinenAnturis for " + (lamsLotjuIds.size()-lamAsemaLotjuIdsWithError.size()) + " LamAsemas");
        if (!lamAsemaLotjuIdsWithError.isEmpty()) {
            log.warn("Fetching LamLaskennallinenAnturis failed for " + lamAsemaLotjuIdsWithError.size() + " LamAsemas");
        }

        // Update sensros of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfLamStations(currentLamAnturiMapByLamLotjuId,
                                           currentLamStationMapByLotjuId,
                                           lamAsemaLotjuIdsWithError);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update LamStations Sensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfLamStations(
            final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamLaskennallinenAnturiMapByLamStationLotjuId,
            final Map<Long, LamStation> currentLamStationMapByLotjuId,
            Set<Long> skipLamAsemasWithLotjuIds) {

        final Map<Long, RoadStationSensor> allSensors =
                roadStationSensorService.findAllRoadStationSensorsMappedByNaturalId(RoadStationType.LAM_STATION);

        final Iterator<Map.Entry<Long, List<LamLaskennallinenAnturiVO>>> entryIter =
                currentLamLaskennallinenAnturiMapByLamStationLotjuId.entrySet().iterator();

        int countAdd = 0;
        int countRemove = 0;
        while (entryIter.hasNext()) {
            final Map.Entry<Long, List<LamLaskennallinenAnturiVO>> entry = entryIter.next();
            final Long lamStationLotjuId = entry.getKey();
            final List<LamLaskennallinenAnturiVO> lamAnturis = entry.getValue();
            entryIter.remove();

            final LamStation lamStation = currentLamStationMapByLotjuId.remove(lamStationLotjuId);

            if (skipLamAsemasWithLotjuIds.contains(lamStationLotjuId)) {
                log.warn("Skip LamStation with lotjuId " + lamStationLotjuId);
            } else if (lamStation == null) {
                log.error("No WeatherStation found for lotjuId " + lamStationLotjuId);
            } else {

                final RoadStation rs = lamStation.getRoadStation();

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

        // remove non exiting sensors from not found lam stations
        for (LamStation lamStation : currentLamStationMapByLotjuId.values()) {
            if (!skipLamAsemasWithLotjuIds.contains(lamStation.getLotjuId())) {
                lamStation.getRoadStation().getRoadStationSensors().clear();
            }
        }


        int notFound = 0;
        for (final List<LamLaskennallinenAnturiVO> values : currentLamLaskennallinenAnturiMapByLamStationLotjuId.values()) {
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
