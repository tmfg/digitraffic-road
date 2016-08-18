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

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.TiesaaAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaLaskennallinenAnturiVO;

@Service
public class WeatherStationsSensorsUpdater extends AbstractWeatherStationUpdater {
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
            log.warn("Not updating WeatherStations metadatas because no lotjuWeatherStationClient defined");
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
                updateSensorsOfWeatherRoadStations(currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap,
                                            currentLotjuIdToWeatherStationsMap);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update WeatherStations RoadStationSensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfWeatherRoadStations(
            final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap,
            final Map<Long, WeatherStation> currentLotjuIdToWeatherStationsMap) {

        final Map<Long, RoadStationSensor> allSensors =
                roadStationSensorService.findAllRoadStationSensorsMappedByNaturalId(RoadStationType.WEATHER_STATION);

        final Iterator<Long> iter = currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.keySet().iterator();

        int countAdd = 0;
        int countRemove = 0;
        while (iter.hasNext()) {
            final Long rwsLotjuId = iter.next();
            final List<TiesaaLaskennallinenAnturiVO> rwsAnturis = currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.get(rwsLotjuId);
            iter.remove();

            final WeatherStation rws = currentLotjuIdToWeatherStationsMap.get(rwsLotjuId);

            if (rws == null) {
                log.error("No WeatherStation found for lotjuId " + rwsLotjuId);
                continue;
            }

            final RoadStation rs = rws.getRoadStation();

            final List<RoadStationSensor> sensors = rws.getRoadStation().getRoadStationSensors();
            final Map<Long, RoadStationSensor> naturalIdToSensorMap = new HashMap<>();
            for (final RoadStationSensor sensor : sensors) {
                naturalIdToSensorMap.put(sensor.getNaturalId(), sensor);
            }

            for (final TiesaaLaskennallinenAnturiVO rwsAnturi : rwsAnturis) {
                final Long sensorNaturalId = Long.valueOf(rwsAnturi.getVanhaId());
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

        int notFound = 0;
        for (final List<TiesaaLaskennallinenAnturiVO> values : currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.values()) {
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
