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

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
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
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.TiesaaAsemaVO;

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
                lamStationService.findAllLamStationsMappedByByMappedByLotjuId();

        final Set<Long> rwsLotjuIds = currentLamStationMapByLotjuId.keySet();

        final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturiMapByLamLotjuId = new HashMap<>();
        final Set<Long> lamAsemaLotjuIdsWithError = new HashSet<>();

        int counter = 0;
        for (final Long lamAsemaLotjuId : rwsLotjuIds) {
            try {
                final List<LamLaskennallinenAnturiVO> anturis = lotjuLamStationClient.getTiesaaLaskennallinenAnturis(lamAsemaLotjuId);
                currentLamAnturiMapByLamLotjuId.put(lamAsemaLotjuId, anturis);
                counter += anturis.size();
            } catch (Exception e) {
                    lamAsemaLotjuIdsWithError.add(lamAsemaLotjuId);
                    log.error("Error while fetching lam's sensors for station lotjuId: " + lamAsemaLotjuId, e);
            }
        }

        log.info("Fetched " + counter + " LamLaskennallinenAnturis for " + (rwsLotjuIds.size()-lamAsemaLotjuIdsWithError.size()) + " LamAsemas");
        if (lamAsemaLotjuIdsWithError.size() > 0) {
            log.warn("Fetching LamLaskennallinenAnturis failed for " + lamAsemaLotjuIdsWithError.size() + " LamAsemas");
        }

        // Update sensros of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfWeatherRoadStations(currentLamAnturiMapByLamLotjuId,
                                                   currentLamStationMapByLotjuId,
                                                   lamAsemaLotjuIdsWithError);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update LamStations Sensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfWeatherRoadStations(
            final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamLaskennallinenAnturiMapByLamStationLotjuId,
            final Map<Long, LamStation> currentLamStationMapByLotjuId,
            Set<Long> skipLamAsemasWithLotjuIds) {

        final Map<Long, RoadStationSensor> allSensors =
                roadStationSensorService.findAllRoadStationSensorsMappedByNaturalId(RoadStationType.LAM_STATION);

        final Iterator<Long> iter = currentLamLaskennallinenAnturiMapByLamStationLotjuId.keySet().iterator();

        int countAdd = 0;
        int countRemove = 0;
        while (iter.hasNext()) {
            final Long lamStationLotjuId = iter.next();
            final List<LamLaskennallinenAnturiVO> rwsAnturis = currentLamLaskennallinenAnturiMapByLamStationLotjuId.get(lamStationLotjuId);
            iter.remove();

            final LamStation lamStation = currentLamStationMapByLotjuId.remove(lamStationLotjuId);

            if (skipLamAsemasWithLotjuIds.contains(lamStationLotjuId)) {
                log.warn("Skip LamStation with lotjuId " + lamStationLotjuId);
                continue;
            } else if (lamStation == null) {
                log.error("No WeatherStation found for lotjuId " + lamStationLotjuId);
                continue;
            }

            final RoadStation rs = lamStation.getRoadStation();

            final List<RoadStationSensor> sensors = lamStation.getRoadStation().getRoadStationSensors();
            final Map<Long, RoadStationSensor> naturalIdToSensorMap = new HashMap<>();
            for (final RoadStationSensor sensor : sensors) {
                naturalIdToSensorMap.put(sensor.getNaturalId(), sensor);
            }

            for (final LamLaskennallinenAnturiVO anturi : rwsAnturis) {
                final Long sensorNaturalId = Long.valueOf(anturi.getVanhaId());
                final RoadStationSensor sensor = naturalIdToSensorMap.remove(sensorNaturalId);
                // road station doesn't have mapping for sensor -> add it
                if ( sensor == null ) {
                    final RoadStationSensor add = allSensors.get(sensorNaturalId);
                    if (add == null) {
                        log.error("No RoadStationSensor found with naturalId " + sensorNaturalId);
                    } else {
                        rs.getRoadStationSensors().add(add);
                        countAdd++;
                        log.info("Add sensor " + add + " for " + rs);
                    }
                }
            }

            // Remove non existing sensors that are left in map
            for (final RoadStationSensor remove : naturalIdToSensorMap.values()) {
                rs.getRoadStationSensors().remove(remove);
                countRemove++;
                log.info("Removed " + remove + " from " + rs);
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

    private static boolean validate(final TiesaaAsemaVO tsa) {
        if (tsa.getVanhaId() == null) {
            log.error(ToStringHelpper.toString(tsa) + " is invalid: has null vanhaId");
            return false;
        }
        return true;
    }

    private void updateRoasWeatherSensorStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER_SENSOR, updateStaticDataStatus);
    }
}
