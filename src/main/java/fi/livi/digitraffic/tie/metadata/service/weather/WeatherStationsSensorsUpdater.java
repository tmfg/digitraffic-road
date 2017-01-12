package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.HashMap;
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
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaLaskennallinenAnturiVO;

@Service
public class WeatherStationsSensorsUpdater extends AbstractWeatherStationAttributeUpdater {
    private static final Logger log = LoggerFactory.getLogger(WeatherStationsSensorsUpdater.class);

    private RoadStationSensorService roadStationSensorService;
    private final WeatherStationService weatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuWeatherStationClient lotjuWeatherStationClient;

    @Autowired
    public WeatherStationsSensorsUpdater(final RoadStationService roadStationService,
                                         final RoadStationSensorService roadStationSensorService,
                                         final WeatherStationService weatherStationService,
                                         final StaticDataStatusService staticDataStatusService,
                                         final LotjuWeatherStationClient lotjuWeatherStationClient) {
        super(roadStationService);
        this.roadStationSensorService = roadStationSensorService;
        this.weatherStationService = weatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuWeatherStationClient = lotjuWeatherStationClient;
    }

    /**
     * Updates all available sensors of weather road stations
     */
    @Transactional
    public boolean updateWeatherStationsSensors() {
        log.info("Update WeatherStations RoadStationSensors start");

        if (lotjuWeatherStationClient == null) {
            log.warn("Not updating WeatherStations Sensors metadata because lotjuWeatherStationClient not defined");
            return false;
        }

        // Update sensors of road stations
        // Get current WeatherStations
        final Map<Long, WeatherStation> currentLotjuIdToWeatherStationsMap =
                weatherStationService.findAllWeatherStationsMappedByLotjuId();
        final Set<Long> rwsLotjuIds = currentLotjuIdToWeatherStationsMap.keySet();
        // Get sensors for current WeatherStations
        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap =
                        lotjuWeatherStationClient.getTiesaaLaskennallinenAnturis(rwsLotjuIds);
        // Update sensros of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfWeatherStations(currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap,
                                               currentLotjuIdToWeatherStationsMap);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update WeatherStations RoadStationSensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfWeatherStations(
            final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap,
            final Map<Long, WeatherStation> currentLotjuIdToWeatherStationsMap) {

        final Map<Long, RoadStationSensor> allSensors =
                roadStationSensorService.findAllRoadStationSensorsMappedByNaturalId(RoadStationType.WEATHER_STATION);

        Iterator<Map.Entry<Long, List<TiesaaLaskennallinenAnturiVO>>> entryIter =
                currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.entrySet().iterator();

        int countAdd = 0;
        int countRemove = 0;
        while (entryIter.hasNext()) {
            Map.Entry<Long, List<TiesaaLaskennallinenAnturiVO>> entry = entryIter.next();
            final Long rwsLotjuId = entry.getKey();
            final List<TiesaaLaskennallinenAnturiVO> rwsAnturis = entry.getValue();
            entryIter.remove();

            final WeatherStation rws = currentLotjuIdToWeatherStationsMap.get(rwsLotjuId);

            if (rws == null) {
                log.error("No WeatherStation found for lotjuId " + rwsLotjuId);
                continue;
            }

            final RoadStation rs = rws.getRoadStation();

            final List<RoadStationSensor> sensors = rs.getRoadStationSensors();
            final Map<Long, RoadStationSensor> naturalIdToCurrentSensorMap = new HashMap<>();
            for (final RoadStationSensor sensor : sensors) {
                naturalIdToCurrentSensorMap.put(sensor.getNaturalId(), sensor);
            }

            for (final TiesaaLaskennallinenAnturiVO rwsAnturi : rwsAnturis) {
                if ( addSensorIfMissing(rs, naturalIdToCurrentSensorMap, rwsAnturi, allSensors) ) {
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

        int notFound = 0;
        for (final List<TiesaaLaskennallinenAnturiVO> values : currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.values()) {
            notFound =+ values.size();
        }
        log.info("RoadStation not found for " + notFound + " TiesaaLaskennallinenAnturis");
        log.info("Sensor removed from road stations " + countRemove);
        log.info("Sensor added to road stations " + countAdd);

        return countAdd > 0 || countRemove > 0;
    }

    private static boolean addSensorIfMissing(RoadStation rs,
                                              Map<Long, RoadStationSensor> naturalIdToCurrentSensorMap,
                                              TiesaaLaskennallinenAnturiVO rwsAnturi,
                                              Map<Long, RoadStationSensor> allSensors) {

        final Long sensorNaturalId = Long.valueOf(rwsAnturi.getVanhaId());
        final RoadStationSensor sensor = naturalIdToCurrentSensorMap.remove(sensorNaturalId);
        // road station doesn't have mapping for sensor -> add it
        if ( sensor == null ) {
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
